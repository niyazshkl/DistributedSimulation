package client;

import assignments.util.mainArgs.ClientArgsProcessor;
import client.modifiedStarter.ASimulation;
import inputport.rpc.GIPCLocateRegistry;
import inputport.rpc.GIPCRegistry;
import server.DistributedRelayer;
import server.GIPCRelayer;
import shared.Constants;
import util.annotations.Tags;
import util.interactiveMethodInvocation.IPCMechanism;
import util.interactiveMethodInvocation.SimulationParametersControllerFactory;
import util.tags.DistributedTags;
import util.trace.port.consensus.ProposalMade;
import util.trace.port.consensus.RemoteProposeRequestSent;
import util.trace.port.rpc.gipc.GIPCObjectLookedUp;
import util.trace.port.rpc.gipc.GIPCRegistryLocated;
import util.trace.port.rpc.rmi.RMIObjectLookedUp;
import util.trace.port.rpc.rmi.RMIRegistryLocated;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

@Tags({DistributedTags.CLIENT_CONFIGURER, DistributedTags.RMI, DistributedTags.GIPC})
public class DefaultClient extends Constants implements Client {

    private String name;

    private String registryHost;
    private int registryPort;

    private String serverHost;
    private int serverPort;

    private int gipcServerPort;

    private Coupler inCoupler;

    private DistributedRelayer rmiServerProxy;
    private GIPCRelayer gipcServerProxy;

    //inherits boolean broadcastIPCMechanism & IPCMechanism ipcMechanism from AnAbstractSimulationParametersBean

    public DefaultClient(){}
    public DefaultClient(String name, String registryHost, int registryPort, String serverHost, int serverPort, int gipcServerPort) {
        this.name = name;
        this.registryHost = registryHost;
        this.registryPort = registryPort;
        this.serverHost = serverHost;
        this.serverPort = serverPort;
        this.inCoupler = new InCoupler(this);
        this.gipcServerPort = gipcServerPort;
        this.setIPCMechanism(IPCMechanism.RMI); //default start point.
    }

    public static void initializeClient(String[] args) {
        String name = ClientArgsProcessor.getClientName(args);

        int registryPort = ClientArgsProcessor.getRegistryPort(args);
        String registryHost = ClientArgsProcessor.getRegistryHost(args);

        String serverHost = ClientArgsProcessor.getServerHost(args);
        int serverPort = ClientArgsProcessor.getServerPort(args);

        int gipcServerPort = ClientArgsProcessor.getGIPCPort(args);

        DefaultClient defaultClient = new DefaultClient(name, registryHost, registryPort, serverHost, serverPort, gipcServerPort);
        defaultClient.configureSimulation();
        defaultClient.rmiProxyJoin();
        defaultClient.gipcProxyJoin();

        defaultClient.boilerPlate();
    }
    private void boilerPlate(){
        //Copied from Comp533All.jar/coupledsims/AStandAloneTwoCoupledHalloweenSimulations::start
        SimulationParametersControllerFactory.getSingleton().addSimulationParameterListener(this);
        SimulationParametersControllerFactory.getSingleton().processCommands();
    }

    public void configureSimulation(){
        // Create and configure simulation
        ASimulation aSimulation = new ASimulation(this);
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
        //get the registry
//        GIPCLocateRegistry.createRegistry(gipcServerPort);// solution??


        GIPCRegistryLocated.newCase(this, serverHost, gipcServerPort, name);
        GIPCRegistry gipcRegistry = GIPCLocateRegistry.getRegistry(serverHost, gipcServerPort, name);
        //lookup the relayer

        GIPCRelayer gipcServerProxy = (GIPCRelayer) gipcRegistry.lookup(GIPCRelayer.class, Constants.GIPC_RELAYER);
        GIPCObjectLookedUp.newCase(this, gipcServerProxy, GIPCRelayer.class, Constants.GIPC_RELAYER, gipcRegistry);
        this.gipcServerProxy = gipcServerProxy;

        //add the client proxy to the relayer
        System.out.println("Joining gipcProxy");
        gipcServerProxy.join(name, (GIPCCoupler) inCoupler);
        System.out.println("Joined gipcProxy");

    }



    public void send(PropertyChangeEvent propertyChangeEvent){
        RemoteProposeRequestSent.newCase(this, this.name, -1, propertyChangeEvent);
        if (this.getIPCMechanism()==IPCMechanism.RMI){
        try {
            this.rmiServerProxy.send(this.name, propertyChangeEvent);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
        }
        else if (this.getIPCMechanism()==IPCMechanism.GIPC){
            this.gipcServerProxy.send(this.name, propertyChangeEvent);
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
            try {
                this.rmiServerProxy.send(name, propertyChangeEvent);
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
