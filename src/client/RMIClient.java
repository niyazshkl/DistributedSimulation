package client;

import assignments.util.mainArgs.ClientArgsProcessor;
import client.modifiedStarter.ASimulation;
import server.DistributedRelayer;
import shared.Constants;
import util.annotations.Tags;
import util.interactiveMethodInvocation.SimulationParametersControllerFactory;
import util.tags.DistributedTags;
import util.trace.port.consensus.RemoteProposeRequestSent;
import util.trace.port.rpc.rmi.RMIObjectLookedUp;
import util.trace.port.rpc.rmi.RMIRegistryLocated;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

@Tags({DistributedTags.CLIENT_CONFIGURER, DistributedTags.RMI})
public class RMIClient extends Constants implements Client {

    private String name;
    private String registryHost;
    private int registryPort;
    private Coupler inCoupler;
    private DistributedRelayer serverProxy;

    public RMIClient(){}
    public RMIClient(String name, String registryHost, int registryPort) {
        this.name = name;
        this.registryHost = registryHost;
        this.registryPort = registryPort;
        this.inCoupler = new InCoupler(this);
    }

    @Override
    public void initializeClient(String[] args) {
        String name = ClientArgsProcessor.getClientName(args);
        int registryPort = ClientArgsProcessor.getRegistryPort(args);
        String registryHost = ClientArgsProcessor.getRegistryHost(args);

        boilerPlate();

        RMIClient rmiClient = new RMIClient(name, registryHost, registryPort);
        rmiClient.configureSimulation();
        rmiClient.joinServer();
    }

    public void configureSimulation(){
        // Create and configure simulation
        ASimulation aSimulation = new ASimulation(this);
        PropertyChangeListener outCoupler = new OutCoupler(this);

        aSimulation.setInCoupler(this.inCoupler); //inCoupler holds an instance of the Processor but does not observe anything.
        aSimulation.setOutCoupler(outCoupler); // outCoupler observes the Processor inside simulation, holds instance of InCoupler -> changes report to relayerProxy
    }

    public void joinServer(){
        try {
            UnicastRemoteObject.exportObject((DistributedCoupler) inCoupler, 0); // I forgot this which didn't send proxy, but sent copy instead

            Registry rmiRegistry = LocateRegistry.getRegistry(registryPort);
            RMIRegistryLocated.newCase(this, registryHost, registryPort, rmiRegistry);

            this.serverProxy =  (DistributedRelayer) rmiRegistry.lookup(Constants.RMI_RELAYER);
            RMIObjectLookedUp.newCase(this, serverProxy, Constants.RMI_RELAYER, rmiRegistry);

            serverProxy.join(this.name, (DistributedCoupler) inCoupler);

        } catch (RemoteException | NotBoundException e) {
            throw new RuntimeException(e);
        }
    }

    public void send(PropertyChangeEvent propertyChangeEvent){
        RemoteProposeRequestSent.newCase(this, this.name, -1, propertyChangeEvent);
        try {
            this.serverProxy.send(this.name, propertyChangeEvent);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }
}
