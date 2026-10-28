package server;

import assignments.util.mainArgs.ServerArgsProcessor;
import client.DistributedCoupler;
import client.GIPCCoupler;
import inputport.nio.manager.NIOManager;
import inputport.nio.manager.NIOManagerFactory;
import inputport.nio.manager.listeners.SocketChannelAcceptListener;
import inputport.nio.manager.listeners.SocketChannelReadListener;
import inputport.nio.manager.listeners.SocketChannelWriteListener;
import inputport.rpc.GIPCLocateRegistry;
import inputport.rpc.GIPCRegistry;
import shared.Constants;
import util.annotations.Tags;
import util.interactiveMethodInvocation.IPCMechanism;
import util.interactiveMethodInvocation.SimulationParametersControllerFactory;
import util.tags.DistributedTags;
import util.trace.port.consensus.ProposalLearnedNotificationSent;
import util.trace.port.consensus.ProposalMade;
import util.trace.port.consensus.RemoteProposeRequestReceived;
import util.trace.port.nio.SocketChannelBound;
import util.trace.port.rpc.gipc.GIPCObjectRegistered;
import util.trace.port.rpc.gipc.GIPCRegistryCreated;
import util.trace.port.rpc.rmi.RMIObjectRegistered;
import util.trace.port.rpc.rmi.RMIRegistryLocated;

import java.beans.PropertyChangeEvent;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

@Tags({DistributedTags.SERVER_CONFIGURER, DistributedTags.RMI, DistributedTags.GIPC, DistributedTags.NIO})
public class DefaultServer extends Constants implements Server, SocketChannelAcceptListener, SocketChannelReadListener, SocketChannelWriteListener {

    private Map<String, DistributedCoupler> clients;
    private Map<String, GIPCCoupler> gipcClients;
    private Relayer relayer;
    private String registryHost;
    private int rmiRegistryPort;
    private int gipcServerPort;
    private int nioServerPort;
    private Registry rmiRegistry;
    private GIPCRegistry gipcRegistry;
    private NIOManager nioManager;
    Thread readThread;
    private boolean hasNioConnection=false;

    private Map<SocketAddress, SocketChannel> clientSocketChannels; //<clientAddress, socketChannel>
    private BlockingQueue<KeyValue<SocketAddress, ByteBuffer>> blockingQueue = new ArrayBlockingQueue<>(2_000); //<clientAddress, command>

    public DefaultServer(String registryHost, int rmiRegistryPort, int gipcServerPort, int nioServerPort) {
        super(); //set tracing is done.
        this.registryHost = registryHost;
        this.rmiRegistryPort = rmiRegistryPort;
        this.gipcServerPort = gipcServerPort;
        this.clients = new HashMap<>();
        this.gipcClients = new HashMap<>();
        this.relayer = new RelayerProxy(this);
        this.nioServerPort = nioServerPort;
        this.nioManager = NIOManagerFactory.getSingleton();
        this.readThread = new Thread(new ServerReader(this,this.blockingQueue));
        this.readThread.setName("Read thread");
        this.setIPCMechanism(IPCMechanism.RMI); //default start point.
    }

    public static void launchServer(String[] args){
        String registryHost = ServerArgsProcessor.getRegistryHost(args);
        int registryPort = ServerArgsProcessor.getRegistryPort(args);
        int gipcServerPort = ServerArgsProcessor.getGIPCServerPort(args);
        int nioServerPort = ServerArgsProcessor.getNIOServerPort(args);
        DefaultServer defaultServer = new DefaultServer(registryHost, registryPort, gipcServerPort, nioServerPort);
        defaultServer.launchServer();
    }

    public void launchServer(){
        this.info();
        this.rmiRegister();
        this.gipcRegister();
        this.nioSetup();
        this.boilerPlate();
    }


    private void info() {
        System.out.println("Registry Host: " + registryHost);
        System.out.println("RMI Registry Port: " + rmiRegistryPort);
        System.out.println("GIPC Server Port: " + gipcServerPort);
    }

    private void rmiRegister(){
        try {
            Registry rmiRegistry = LocateRegistry.getRegistry(rmiRegistryPort);

            RMIRegistryLocated.newCase(this, registryHost, rmiRegistryPort, rmiRegistry);
            this.rmiRegistry = rmiRegistry;
            DistributedRelayer proxy =
                    (DistributedRelayer) UnicastRemoteObject.exportObject(
                            (DistributedRelayer) relayer, 0
                    );

            rmiRegistry.rebind(
                    Constants.RMI_RELAYER,
                    proxy
            );

            RMIObjectRegistered.newCase(this, Constants.RMI_RELAYER, -1, rmiRegistry);

        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void ipcMechanism(IPCMechanism newValue){
        ProposalMade.newCase(this, "ipc_mechanism", -1.0F, newValue);
        var oldValue = getIPCMechanism();
        this.setIPCMechanism(newValue);
        if (isBroadcastMetaState()){
            //broadcast using rmiProxy always if the broadcast is set active.
            PropertyChangeEvent propertyChangeEvent = new PropertyChangeEvent(this, "ipc_mechanism",oldValue,getIPCMechanism());
            this.broadcast("SEVER BOSS LOL", propertyChangeEvent);
        }
    }

    private void boilerPlate(){
        //Copied from Comp533All.jar/coupledsims/AStandAloneTwoCoupledHalloweenSimulations::start
        SimulationParametersControllerFactory.getSingleton().addSimulationParameterListener(this);
        SimulationParametersControllerFactory.getSingleton().processCommands();
    }

    private void gipcRegister(){
        GIPCRegistryCreated.newCase(this,gipcServerPort);
        GIPCRegistry gipcRegistry = GIPCLocateRegistry.createRegistry(gipcServerPort);
        this.gipcRegistry = gipcRegistry;
        //add the proxy to this one too.
        GIPCObjectRegistered.newCase(this, Constants.GIPC_RELAYER, this.relayer, gipcRegistry);
        gipcRegistry.rebind(Constants.GIPC_RELAYER, this.relayer);

        gipcRegistry.getInputPort().addConnectionListener(this);
    }

    @Override
    public void broadcast(String senderName, PropertyChangeEvent event) {
            ProposalMade.newCase(this, senderName, -1, event);
            if (event.getPropertyName().equals("ipc_mechanism")) {//always RMI Client proxies (this.clients)
                for (Map.Entry<String, DistributedCoupler> entry : this.clients.entrySet()) {
                    if (!entry.getKey().equals(senderName)) {
                        ProposalLearnedNotificationSent.newCase(this, entry.getKey(), -1, event);
                        //no matter what always RMI
                        try {
                            entry.getValue().receiveChange(event);
                        } catch (RemoteException e) {
                            throw new RuntimeException(e);
                        }

                    }
                }
            } else{
                if (this.getIPCMechanism()==IPCMechanism.RMI){
                    for (Map.Entry<String, DistributedCoupler> entry : this.clients.entrySet()) { //RMI Client proxies (this.clients)
                        if (!entry.getKey().equals(senderName)) {
                            ProposalLearnedNotificationSent.newCase(this, entry.getKey(), -1, event);

                            //no matter what always RMI
                            try {
                                entry.getValue().receiveChange(event);
                            } catch (RemoteException e) {
                                throw new RuntimeException(e);
                            }

                        }
                    }
                } else if (this.getIPCMechanism()==IPCMechanism.GIPC){
                    for (Map.Entry<String, GIPCCoupler> entry : this.gipcClients.entrySet()){
                        if (!entry.getKey().equals(senderName)) {
                            ProposalLearnedNotificationSent.newCase(this, entry.getKey(), -1, event);
                            entry.getValue().receiveChange(event);
                        }
                    }
                }


            }

    }

    @Override
    public void addClient(String name, DistributedCoupler inCoupler){
        this.clients.put(name, inCoupler); //overwrites previous incoupler of a client
    }
    @Override
    public void addGipcClient(String name, GIPCCoupler inCoupler){
        this.gipcClients.put(name, inCoupler); //overwrites previous incoupler of a client
    }


    private void nioSetup(){
        try {
            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            InetSocketAddress inetSocketAddress = new InetSocketAddress(nioServerPort);
            serverSocketChannel.bind(inetSocketAddress);
            SocketChannelBound.newCase(this, serverSocketChannel, inetSocketAddress);
            this.clientSocketChannels = new HashMap<>();
            this.nioManager.enableListenableAccepts(serverSocketChannel, SelectionKey.OP_READ, this); //already does nioManager.enableReads(socketChannel) upon connection.
//            this.nioManager.addReadListener(serverSocketChannel, this);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    @Override
    public void addClientNioSocketChannel(SocketChannel clientSocketChannel){
        try {
            SocketAddress clientAddress = clientSocketChannel.getRemoteAddress();
            this.clientSocketChannels.put(clientAddress, clientSocketChannel);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void nioBroadcast(SocketAddress client, ByteBuffer command) {
        ProposalMade.newCase(this, client.toString(), -1, command);
        for (Map.Entry<SocketAddress, SocketChannel> entry: this.clientSocketChannels.entrySet()){
            SocketAddress currentClient = entry.getKey();
            if (!currentClient.equals(client)){
                //broadcast here
                nioManager.write(entry.getValue(), command, this);
            }
        }
    }

    @Override
    public void socketChannelAccepted(ServerSocketChannel serverSocketChannel, SocketChannel socketChannel) {
        //store the client endpoint (socketChannel)
        this.addClientNioSocketChannel(socketChannel);
        if (!this.hasNioConnection) this.readThread.start();
        this.hasNioConnection = true;
//        this.nioManager.enableReads(socketChannel);
        this.nioManager.addReadListener(socketChannel, this);
    }

    @Override
    public void socketChannelRead(SocketChannel socketChannel, ByteBuffer command, int i) {
        //put in a blocking collection
        try {
            RemoteProposeRequestReceived.newCase(this, "command", -1, command);
            KeyValue<SocketAddress, ByteBuffer> data = new KeyValue<>(socketChannel.getRemoteAddress(), command);
            this.blockingQueue.add(data);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void written(SocketChannel socketChannel, ByteBuffer byteBuffer, int i) {
        //go over each client endpoint (socketChannel) and then send it to them and they will put in their blocking collection.
        ProposalLearnedNotificationSent.newCase(this, "command", -1,byteBuffer);
    }
}
