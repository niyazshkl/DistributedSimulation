package client;

import server.DistributedRelayer;
import util.annotations.Tags;
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
public class RMIClient {

    private String name;
    private DistributedRelayer serverProxy;

    public RMIClient(String name) {
        this.name = name;
    }

    public static void main(String[] args){
        String name = ClientArgsProcessor.getClientName(args);
        RMIClient rmiClient = new RMIClient(name);
        rmiClient.init(args);
    }

    public void init(String[] args){
        //create client
        int registryPort = ClientArgsProcessor.getRegistryPort(args);
        //create simulation
        ASimulation aSimulation = new ASimulation(this.name);

        //get the inCoupler proxy

        DistributedCoupler inCouplerProxy = aSimulation.getInCoupler();

        //lookup the rmiregistry
        try {
            UnicastRemoteObject.exportObject(inCouplerProxy, 0); // I forgot this which didnt send proxy, but sent copy instead

            Registry rmiRegistry = LocateRegistry.getRegistry(registryPort);
            RMIRegistryLocated.newCase(this, ClientArgsProcessor.getRegistryHost(args), registryPort, rmiRegistry);

            this.serverProxy =  (DistributedRelayer) rmiRegistry.lookup("RELAYER");
            RMIObjectLookedUp.newCase(this, serverProxy, "RELAYER", rmiRegistry);

            serverProxy.join(this.name, inCouplerProxy);

            PropertyChangeListener outCoupler = new OutCoupler(this);
            aSimulation.setOutCoupler(outCoupler); //adds outCoupler as a observer internally





        } catch (RemoteException e) {
            throw new RuntimeException(e);
        } catch (NotBoundException e) {
            throw new RuntimeException(e);
        }
        //lookup the serverproxyrelyer

        //join inCoupler with it

        //outCoupler needs to be creted and then use simulation.outCoupler

        //I think thats it?

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
