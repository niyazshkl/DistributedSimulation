package server;

import assignments.util.mainArgs.ServerArgsProcessor;
import client.DistributedCoupler;
import client.GIPCCoupler;
import consensus.ProposalFeedbackKind;
import inputport.nio.manager.NIOManager;
import inputport.nio.manager.NIOManagerFactory;
import inputport.nio.manager.listeners.SocketChannelAcceptListener;
import inputport.nio.manager.listeners.SocketChannelReadListener;
import inputport.nio.manager.listeners.SocketChannelWriteListener;
import inputport.rpc.GIPCLocateRegistry;
import inputport.rpc.GIPCRegistry;
import shared.Constants;
import shared.NIOUtilities;
import util.annotations.Tags;
import util.interactiveMethodInvocation.ConsensusAlgorithm;
import util.interactiveMethodInvocation.IPCMechanism;
import util.interactiveMethodInvocation.SimulationParametersControllerFactory;
import util.tags.DistributedTags;
import util.trace.TraceableInfo;
import util.trace.port.consensus.*;
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
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

@Tags({DistributedTags.SERVER_CONFIGURER, DistributedTags.RMI, DistributedTags.GIPC, DistributedTags.NIO, DistributedTags.ATOMIC_BROADCAST, DistributedTags.TWO_PHASE_COMMIT})
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
        this.relayer = new DefaultRelayer(this);
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
    public void addClient(String name, DistributedCoupler inCoupler){
        this.clients.put(name, inCoupler); //overwrites previous incoupler of a client
    }

    @Override
    public void ipcMechanism(IPCMechanism newValue){
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
    public void localProcessingOnly(boolean newValue){
        super.localProcessingOnly(newValue);
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

    private void nioBroadcast(PropertyChangeEvent event, TraceableInfo trace) {
        ByteBuffer command =  NIOUtilities.stringToByteBuffer(event.getNewValue().toString());
        ProposalMade.newCase(this, "client.toString()", -1, command);
        for (SocketChannel clientSocketChannel: this.clientSocketChannels.values()){
            //broadcast to every nio client socketChannel
            printAppropriateTrace(event,trace);
            nioManager.write(clientSocketChannel, command, this);
        }
    }

    @Override
    public void broadcast(String senderName, PropertyChangeEvent event) {
        ProposalMade.newCase(this, senderName, -1, event);
        if (isMetaStateEvent(event))
        {
            if (event.getPropertyName().equals("ipc_mechanism")) this.setIPCMechanism((IPCMechanism) event.getNewValue());
            else if (event.getPropertyName().equals("broadcast_mode")) this.setAtomicBroadcast((Boolean) event.getNewValue());
            rmiInCouplerBroadcast(senderName, event, aProposalLearnedNotificationSent()); //always RMI Client proxies (this.clients) no matter what for ipc_mechanism and atomicBroadcast
            return; //no need to handle atomicBroadcast for metastate (thats extra credit will handle later)
        }

        if (isAtomicBroadcast()) doAtomicBroadcast(senderName, event);
        else doBroadcast(senderName,event, aProposalLearnedNotificationSent());
    }

    private boolean isMetaStateEvent(PropertyChangeEvent event){
        return event.getPropertyName().equals("ipc_mechanism") || event.getPropertyName().equals("broadcast_mode");
    }

    private void doBroadcast(String senderName, PropertyChangeEvent event, TraceableInfo trace){
        if (this.getIPCMechanism()==IPCMechanism.RMI){
            rmiInCouplerBroadcast(senderName, event, trace);
        } else if (this.getIPCMechanism()==IPCMechanism.GIPC){
            gipcInCouplerBroadcast(senderName, event, trace);
        }  else if (this.getIPCMechanism()==IPCMechanism.NIO){
            nioBroadcast(event, trace);
        }

        //NIO will send it directly to the serverSocketChannel so no need to handle
    }


    private void gipcInCouplerBroadcast(String senderName, PropertyChangeEvent event, TraceableInfo trace) {
        for (Map.Entry<String, GIPCCoupler> entry : this.gipcClients.entrySet()){
            if (!entry.getKey().equals(senderName)) {
                printAppropriateTrace(event,trace);
                entry.getValue().receiveChange(event);
            }
        }
    }

    private void rmiInCouplerBroadcast(String senderName, PropertyChangeEvent event, TraceableInfo trace) {
        for (Map.Entry<String, DistributedCoupler> entry : this.clients.entrySet()) {
            if (!entry.getKey().equals(senderName)) {
                //no matter what always RMI
                try {
                    printAppropriateTrace(event, trace);
                    entry.getValue().receiveChange(event);
                } catch (RemoteException e) {
                    throw new RuntimeException(e);
                }

            }
        }
    }

    private void rmiInCouplerBroadcast(PropertyChangeEvent event, TraceableInfo trace) {
        //sends to all no matter the sender
        for (Map.Entry<String, DistributedCoupler> entry : this.clients.entrySet()) {
//                ProposalLearnedNotificationSent.newCase(this, entry.getKey(), -1, event);
                //no matter what always RMI
                try {
                    printAppropriateTrace(event, trace);
                    if (trace instanceof ProposalAcceptRequestSent) entry.getValue().receiveProposalAcceptRequest(event);
                    else entry.getValue().receiveChange(event);
//                    ProposalAcceptRequestSent.newCase(this, "ProposalAcceptRequest", -1, event);
                } catch (RemoteException e) {
                    throw new RuntimeException(e);
                }
        }
    }

    private void printAppropriateTrace(PropertyChangeEvent event, TraceableInfo trace) {
        if (trace instanceof ProposalLearnedNotificationSent){
            ProposalLearnedNotificationSent.newCase(this, "ProposalLearnedNotification", -1, event);
        } else if (trace instanceof ProposalAcceptRequestSent){
            ProposalAcceptRequestSent.newCase(this, "ProposalAcceptRequest", -1, event);
        }
    }
    private ProposalAcceptRequestSent aProposalAcceptRequestSent(){
        return new ProposalAcceptRequestSent(null,null,null,-1,null);
    }
    private ProposalLearnedNotificationSent aProposalLearnedNotificationSent(){
        return new ProposalLearnedNotificationSent(null,null,null,-1,null);
    }
    private void doAtomicBroadcast(String senderName, PropertyChangeEvent event){ //only needed during atomic broadcast
        //phase 1
        /*ProposalAcceptRequest
        Go through each client and send to each client a proposal asking if they are okay with the new value
        Receive the value then decide
        Always done using RMI according to instructions
        */
        rmiInCouplerBroadcast(event, aProposalAcceptRequestSent());
    }

    private final Map<String, ProposalFeedbackKind> clientProposalAcceptRequestResponses = new HashMap<>();
 @Override
    public synchronized void handleProposalAcceptRequestResponse(String sender, PropertyChangeEvent anEvent, ProposalFeedbackKind response) {
        //need thread safety because rmi proxy relayer is non-deterministic
            clientProposalAcceptRequestResponses.put(sender, response);

            if (clientProposalAcceptRequestResponses.entrySet().size()==clients.size()) {
             Object agreedValue = makeDecision(anEvent);
             clientProposalAcceptRequestResponses.clear();
             PropertyChangeEvent agreedEvent = new PropertyChangeEvent(this, anEvent.getPropertyName(), anEvent.getOldValue(), agreedValue);
             this.proposalAcceptPhase2("Server_NAME", agreedEvent);
            }

        /*
        Called every time that the client sends a response
        Wait for all responses
        makeDecision
         */

    }

    private Object makeDecision(PropertyChangeEvent originalEvent){
        /*
        Algirhtm: is the value accepted by AlL(techinically most) clients?
        If so, then set value to the value thought of
        if not, then set value to null
        no matter what still have to send even if null
         */
        for (ProposalFeedbackKind feedbackKind: clientProposalAcceptRequestResponses.values()) if (feedbackKind!=ProposalFeedbackKind.SUCCESS) return null;
        return originalEvent.getNewValue();
    }

    private void proposalAcceptPhase2(String sender, PropertyChangeEvent anEvent){ //only needed during atomic broadcast
        //ProposalAcceptedNotification
        //straight forward, just like broadcast but the difference is sent to ALL because the sender also did not update!
        ProposalAcceptedNotificationSent.newCase(this, anEvent.getPropertyName(), -1, anEvent, ProposalFeedbackKind.SUCCESS);
        this.doBroadcast(sender, anEvent, aProposalAcceptRequestSent()); //automatically does based on IPC Mechanism
    }

    @Override
    public void atomicBroadcast(boolean newValue){
//        super.atomicBroadcast(newValue);
        var oldValue = isAtomicBroadcast();
        setAtomicBroadcast(newValue);
        if (isBroadcastMetaState()){
            //broadcast using rmiProxy always if the broadcast is set active.
            PropertyChangeEvent propertyChangeEvent = new PropertyChangeEvent(this, "broadcast_mode",oldValue,isAtomicBroadcast());
            this.broadcast("SEVER BOSS LOL", propertyChangeEvent);
        }
    }

    @Override
    public void consensusAlgorithm(ConsensusAlgorithm newValue) {
        this.setConsensusAlgorithm(newValue);
        if (newValue==ConsensusAlgorithm.CENTRALIZED_SYNCHRONOUS) atomicBroadcast(true);

    }

}
