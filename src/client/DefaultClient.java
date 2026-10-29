package client;

import assignments.util.mainArgs.ClientArgsProcessor;
import client.modifiedStarter.ASimulation;
import client.modifiedStarter.Simulation;
import consensus.ProposalFeedbackKind;
import inputport.nio.manager.NIOManager;
import inputport.nio.manager.NIOManagerFactory;
import inputport.nio.manager.listeners.SocketChannelConnectListener;
import inputport.nio.manager.listeners.SocketChannelReadListener;
import inputport.nio.manager.listeners.SocketChannelWriteListener;
import inputport.rpc.GIPCLocateRegistry;
import inputport.rpc.GIPCRegistry;
import server.DistributedRelayer;
import server.GIPCRelayer;
import shared.Constants;
import shared.NIOUtilities;
import util.annotations.Tags;
import util.interactiveMethodInvocation.ConsensusAlgorithm;
import util.interactiveMethodInvocation.IPCMechanism;
import util.interactiveMethodInvocation.SimulationParametersControllerFactory;
import util.tags.DistributedTags;
import util.trace.port.PerformanceExperimentEnded;
import util.trace.port.PerformanceExperimentStarted;
import util.trace.port.consensus.*;
import util.trace.port.rpc.gipc.GIPCObjectLookedUp;
import util.trace.port.rpc.gipc.GIPCRegistryLocated;
import util.trace.port.rpc.rmi.RMIObjectLookedUp;
import util.trace.port.rpc.rmi.RMIRegistryLocated;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

@Tags({DistributedTags.CLIENT_CONFIGURER, DistributedTags.RMI, DistributedTags.GIPC, DistributedTags.NIO,DistributedTags.ATOMIC_BROADCAST, DistributedTags.TWO_PHASE_COMMIT})
public class DefaultClient extends Constants implements Client, SocketChannelConnectListener, SocketChannelWriteListener, SocketChannelReadListener {

    private String name;

    private String registryHost;
    private int registryPort;

    private String serverHost;
    private int serverPort;

    private int gipcServerPort;
    private int nioServerPort;
    private NIOManager nioManager;
    private Thread readThread;

    private Coupler inCoupler;

    private DistributedRelayer rmiServerProxy;
    private GIPCRelayer gipcServerProxy;
    private SocketChannel socketChannel;

    private BlockingQueue<String> commands;
    private Simulation simulation;

    protected int NUM_EXPERIMENT_COMMANDS = 500;

    //inherits boolean broadcastIPCMechanism & IPCMechanism ipcMechanism from AnAbstractSimulationParametersBean

    public DefaultClient(){}
    public DefaultClient(String name, String registryHost, int registryPort, String serverHost, int serverPort, int gipcServerPort, int nioServerPort) {
        this.name = name;
        this.registryHost = registryHost;
        this.registryPort = registryPort;
        this.serverHost = serverHost;
        this.serverPort = serverPort;
        this.inCoupler = new InCoupler(this);
        this.gipcServerPort = gipcServerPort;
        this.nioServerPort = nioServerPort;
        this.nioManager = NIOManagerFactory.getSingleton();
        this.commands = new ArrayBlockingQueue<>(2_000);
        this.ipcMechanism = IPCMechanism.RMI;//default start point.
    }

    public static void initializeClient(String[] args) {
        String name = ClientArgsProcessor.getClientName(args);

        int registryPort = ClientArgsProcessor.getRegistryPort(args);
        String registryHost = ClientArgsProcessor.getRegistryHost(args);

        String serverHost = ClientArgsProcessor.getServerHost(args);
        int serverPort = ClientArgsProcessor.getServerPort(args);

        int gipcServerPort = ClientArgsProcessor.getGIPCPort(args);
        int nioServerPort = ClientArgsProcessor.getNIOServerPort(args);

        DefaultClient defaultClient = new DefaultClient(name, registryHost, registryPort, serverHost, serverPort, gipcServerPort, nioServerPort);
        defaultClient.configureSimulation();
        defaultClient.rmiProxyJoin();
        defaultClient.gipcProxyJoin();
        defaultClient.nioConnect();
        defaultClient.boilerPlate();
    }
    private void nioConnect(){
        try {
            this.socketChannel = SocketChannel.open();
            InetAddress inetAddress = InetAddress.getByName(this.serverHost);
            this.nioManager.connect(socketChannel,inetAddress, this.nioServerPort, SelectionKey.OP_READ, this);
//            this.nioManager.enableReads(socketChannel);
            this.nioManager.addReadListener(socketChannel,this);
            this.readThread = new Thread(new ClientReader(this, this.commands));
            this.readThread.setName("Read Thread");
            this.readThread.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
    private void boilerPlate(){
        //Copied from Comp533All.jar/coupledsims/AStandAloneTwoCoupledHalloweenSimulations::start
        SimulationParametersControllerFactory.getSingleton().addSimulationParameterListener(this);
        SimulationParametersControllerFactory.getSingleton().processCommands();
    }

    public void configureSimulation(){
        // Create and configure simulation
        ASimulation aSimulation = new ASimulation(this);
        this.simulation = aSimulation;
        PropertyChangeListener outCoupler = new OutCoupler(this);

        aSimulation.setInCoupler(this.inCoupler); //inCoupler holds an instance of the Processor but does not observe anything.
        aSimulation.setOutCoupler(outCoupler); // outCoupler observes the Processor inside simulation, holds instance of InCoupler -> changes report to relayerProxy
    }

    public void rmiProxyJoin(){
        try {
            UnicastRemoteObject.exportObject((DistributedCoupler) inCoupler, 0); // I forgot this which didn't send proxy, but sent copy instead

            Registry rmiRegistry = LocateRegistry.getRegistry(registryPort);
            RMIRegistryLocated.newCase(this, registryHost, registryPort, rmiRegistry);

            this.rmiServerProxy =  (DistributedRelayer) rmiRegistry.lookup(Constants.RMI_RELAYER);
            RMIObjectLookedUp.newCase(this, rmiServerProxy, Constants.RMI_RELAYER, rmiRegistry);

            rmiServerProxy.join(this.name, (DistributedCoupler) inCoupler);

        } catch (RemoteException | NotBoundException e) {
            throw new RuntimeException(e);
        }
    }

    public void gipcProxyJoin(){

        GIPCRegistryLocated.newCase(this, serverHost, gipcServerPort, name);
        GIPCRegistry gipcRegistry = GIPCLocateRegistry.getRegistry(serverHost, gipcServerPort, name);

        GIPCRelayer gipcServerProxy = (GIPCRelayer) gipcRegistry.lookup(GIPCRelayer.class, Constants.GIPC_RELAYER);
        GIPCObjectLookedUp.newCase(this, gipcServerProxy, GIPCRelayer.class, Constants.GIPC_RELAYER, gipcRegistry);
        this.gipcServerProxy = gipcServerProxy;

        gipcServerProxy.join(name, (GIPCCoupler) inCoupler);
    }

    @Override
    public void localProcessingOnly(boolean newValue){
        super.localProcessingOnly(newValue);
    }
    private void smallDelay(int seconds){
        try {
            Thread.sleep(seconds*1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    public void send(PropertyChangeEvent propertyChangeEvent){
        if (isLocalProcessingOnly()) return;
        if (this.getIPCMechanism()==IPCMechanism.RMI){
        try {
            RemoteProposeRequestSent.newCase(this, this.name, -1, propertyChangeEvent);
            smallDelay(5);
            this.rmiServerProxy.receive(this.name, propertyChangeEvent);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
        }
        else if (this.getIPCMechanism()==IPCMechanism.GIPC){
            RemoteProposeRequestSent.newCase(this, this.name, -1, propertyChangeEvent);
            smallDelay(5);
            this.gipcServerProxy.receive(this.name, propertyChangeEvent);
        } else if (this.getIPCMechanism()==IPCMechanism.NIO) {
            RemoteProposeRequestSent.newCase(this, this.name, -1, propertyChangeEvent);
            smallDelay(5);
            this.nioManager.write(this.socketChannel, NIOUtilities.stringToByteBuffer(propertyChangeEvent.getNewValue().toString()), this);
        }
    }

    public void sendCommand(String command){
            ProposalMade.newCase(this, this.name, -1, command);
            this.nioManager.write(this.socketChannel, NIOUtilities.stringToByteBuffer(command), this);
    }
    @Override
    public void simulationCommand(String command){this.simulation.simulationCommand(command);
    }
    @Override
    public void experimentInput() {
        long aStartTime = System.currentTimeMillis();
        PerformanceExperimentStarted.newCase(this, aStartTime, this.NUM_EXPERIMENT_COMMANDS);
        boolean anOldValue = this.isTrace();
        this.trace(false);

        for(int i = 0; i < this.NUM_EXPERIMENT_COMMANDS; ++i) {
            this.simulation.simulationCommand("move 1 -1");
            this.simulation.simulationCommand("undo");
        }

        this.trace(anOldValue);
        long anEndTime = System.currentTimeMillis();
        PerformanceExperimentEnded.newCase(this, aStartTime, anEndTime, anEndTime - aStartTime, this.NUM_EXPERIMENT_COMMANDS);
    }
    @Override
    public void ipcMechanism(IPCMechanism newValue){
        var oldValue = getIPCMechanism();
        this.setIPCMechanism(newValue);
        if (isBroadcastMetaState()){// if the broadcast is set active
            //broadcast IpcMechanism replica object always using rmiProxy (never any other IPC Mechanism).
            PropertyChangeEvent propertyChangeEvent = new PropertyChangeEvent(this, "ipc_mechanism",oldValue,getIPCMechanism());
            try {
                RemoteProposeRequestSent.newCase(this, this.name, -1, propertyChangeEvent);
                smallDelay(5);
                this.rmiServerProxy.receive(name, propertyChangeEvent);
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void connected(SocketChannel socketChannel) {
        this.nioManager.enableReads(socketChannel);
//        this.nioManager.addReadListener(socketChannel,this);
    }

    @Override
    public void notConnected(SocketChannel socketChannel, Exception e) {
        e.printStackTrace();
    }

    @Override
    public void socketChannelRead(SocketChannel socketChannel, ByteBuffer byteBuffer, int i) {
        String command = NIOUtilities.byteBufferToString(byteBuffer);
        try {
            RemoteProposeRequestReceived.newCase(this, socketChannel.getRemoteAddress().toString(), -1, command);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.commands.add(command);
    }

    @Override
    public void written(SocketChannel socketChannel, ByteBuffer byteBuffer, int sendId) {
        String sentString = NIOUtilities.byteBufferToString(byteBuffer);
        ProposalLearnedNotificationSent.newCase(this, socketChannel.toString(), -1,sentString);
        smallDelay(5);
    }

    @Override
    public void setAtomicBroadcast(Boolean atomicBroadcast){
        ProposedStateSet.newCase(this, "broadcast_mode", -1.0F, atomicBroadcast);
        if (atomicBroadcast!=null){
        this.atomicBroadcast = atomicBroadcast;
        this.simulation.atomicBroadcast(atomicBroadcast);
        }
    }

    @Override
    public synchronized void setIPCMechanism(IPCMechanism newValue) {
        ProposedStateSet.newCase(this, "ipc_mechanism", -1.0F, newValue);
        if (newValue!=null){
        this.ipcMechanism = newValue;
        }
    }


    @Override
    public void atomicBroadcast(boolean newValue){
        boolean oldValue = isAtomicBroadcast();
        setAtomicBroadcast(newValue);
//        super.atomicBroadcast(newValue);
        if (isBroadcastMetaState()){// if the broadcast is set active
            //broadcast meta state replica object always using rmiProxy (never any other IPC Mechanism).
            PropertyChangeEvent propertyChangeEvent = new PropertyChangeEvent(this, "broadcast_mode",oldValue,isAtomicBroadcast());
            try {
                RemoteProposeRequestSent.newCase(this, this.name, -1, propertyChangeEvent);
                smallDelay(5);
                this.rmiServerProxy.receive(name, propertyChangeEvent);
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void consensusAlgorithm(ConsensusAlgorithm newValue) {
        this.setConsensusAlgorithm(newValue);
        if (newValue==ConsensusAlgorithm.CENTRALIZED_SYNCHRONOUS) atomicBroadcast(true);
    }

    @Override
    public void handleProposalAcceptRequest(PropertyChangeEvent anEvent) {
        /*
        Some algorithm to determine response: for now just a dummy one is to always accept
        Send it back to the server using relayer proxy (RMI Only)
         */
        ProposalFeedbackKind response = determineProposalAcceptRequestReponse(anEvent);
        sendProposalAcceptResponse(anEvent, response);
    }

    private void sendProposalAcceptResponse(PropertyChangeEvent anEvent, ProposalFeedbackKind response) {
        //according to instructions, always done using RMI
        try {
            ProposalAcceptRequestSent.newCase(this, "ProposalAcceptRequest", -1, anEvent);
            smallDelay(5);
            this.rmiServerProxy.receiveProposalAcceptResponse(this.name, anEvent, response);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    private ProposalFeedbackKind determineProposalAcceptRequestReponse(PropertyChangeEvent anEvent){return ProposalFeedbackKind.SUCCESS;}

}
