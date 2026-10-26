package server;

import assignments.util.mainArgs.ServerArgsProcessor;

import client.DistributedCoupler;
import client.GIPCCoupler;
import inputport.ConnectionType;
import inputport.rpc.GIPCLocateRegistry;
import inputport.rpc.GIPCRegistry;
import shared.Constants;
import util.interactiveMethodInvocation.IPCMechanism;
import util.interactiveMethodInvocation.SimulationParametersControllerFactory;
import util.trace.port.consensus.ProposalLearnedNotificationSent;
import util.annotations.Tags;
import util.tags.DistributedTags;
import util.trace.port.consensus.ProposalMade;
import util.trace.port.rpc.gipc.GIPCObjectRegistered;
import util.trace.port.rpc.gipc.GIPCRegistryCreated;
import util.trace.port.rpc.rmi.RMIObjectRegistered;
import util.trace.port.rpc.rmi.RMIRegistryLocated;

import java.beans.PropertyChangeEvent;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;
@Tags({DistributedTags.SERVER_CONFIGURER, DistributedTags.RMI, DistributedTags.GIPC})
public class DefaultServer extends Constants implements Server{

    private Map<String, DistributedCoupler> clients;
    private Map<String, GIPCCoupler> gipcClients;
    private Relayer relayer;
    private String registryHost;
    private int rmiRegistryPort;
    private int gipcServerPort;
    private Registry rmiRegistry;
    private GIPCRegistry gipcRegistry;

    public DefaultServer(String registryHost, int rmiRegistryPort, int gipcServerPort) {
        super(); //set tracing is done.
        this.registryHost = registryHost;
        this.rmiRegistryPort = rmiRegistryPort;
        this.gipcServerPort = gipcServerPort;
        this.clients = new HashMap<>();
        this.gipcClients = new HashMap<>();
        this.relayer = new RelayerProxy(this);
        this.setIPCMechanism(IPCMechanism.RMI); //default start point.
    }

    public void setRegistryHost(String registryHost) {
        this.registryHost = registryHost;
    }

    public void setRmiRegistryPort(int rmiRegistryPort) {
        this.rmiRegistryPort = rmiRegistryPort;
    }

    public static void launchServer(String[] args){
        String registryHost = ServerArgsProcessor.getRegistryHost(args);
        int registryPort = ServerArgsProcessor.getRegistryPort(args);
        int gipcServerPort = ServerArgsProcessor.getGIPCServerPort(args);
        DefaultServer defaultServer = new DefaultServer(registryHost, registryPort, gipcServerPort);
        defaultServer.launchServer();
    }

    public void launchServer(){
        this.info();
        this.rmiRegister();
        this.gipcRegister();
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

    public void addClient(String name, DistributedCoupler inCoupler){
        this.clients.put(name, inCoupler); //overwrites previous incoupler of a client
    }
    public void addGipcClient(String name, GIPCCoupler inCoupler){
        this.gipcClients.put(name, inCoupler); //overwrites previous incoupler of a client
    }


}
