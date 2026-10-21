package client;

import assignments.util.inputParameters.AnAbstractSimulationParametersBean;
import assignments.util.mainArgs.ClientArgsProcessor;
import server.DistributedRelayer;
import server.RMIServer;
import util.trace.factories.FactoryTraceUtility;
import util.trace.misc.ThreadDelayed;
import util.trace.port.PortTraceUtility;
import util.trace.port.consensus.ConsensusTraceUtility;
import util.trace.port.nio.NIOTraceUtility;
import util.trace.port.rpc.rmi.RMIObjectLookedUp;
import util.trace.port.rpc.rmi.RMIObjectRegistered;
import util.trace.port.rpc.rmi.RMITraceUtility;

import javax.naming.Name;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class RMIClient extends AnAbstractSimulationParametersBean {
    public static int num = 0;

    public static void main(String[] args){

        new RMIClient().createClient(args);
    }

    public void createClient(String[] args){
        setTracing();

        String name = ClientArgsProcessor.getClientName(args);

        String registryHost = ClientArgsProcessor.getRegistryHost(args);
        int registryPort = ClientArgsProcessor.getRegistryPort(args);

        String serverHost = ClientArgsProcessor.getServerHost(args);
        int serverPort = ClientArgsProcessor.getServerPort(args);

        String headLess = ClientArgsProcessor.getHeadless(args);

        //rfom the registety get the relayer and add the outcoupler (should be proxied)

        try {
           Registry rmiRegistry = LocateRegistry.getRegistry(registryPort);
           String objectname = "RELAYER";
           Remote remoteObject = rmiRegistry.lookup(objectname);
           RMIObjectLookedUp.newCase(this,  remoteObject, objectname, rmiRegistry);
           DistributedRelayer relayer = (DistributedRelayer) remoteObject;

           //creaete the outcoupler proxy
            DistributedCoupler outCoupler = new OutCoupler();

            Client client = new Client(name, outCoupler);

            UnicastRemoteObject.exportObject( (DistributedPropertyChangeListener) outCoupler, 0); //ensures the same outCoupler object is shared, not copy

            System.out.println("EXPORTED");

            client.setRelayer(relayer);
            if (RMIClient.num != 0)
            {
                client.setCommandX(400);
                client.setCommandY(0);
                client.setSimulationHeight(400);
                client.setSimulationWidth(765);
                client.setSimulationX(400);
                client.setSimulationY(0);
            }
            client.buildSimulation();

            System.out.println(client);


            relayer.join(name, (DistributedPropertyChangeListener) outCoupler);
            System.out.println("JOINED");

        } catch (RemoteException e) {
            throw new RuntimeException(e);
        } catch (NotBoundException e) {
            throw new RuntimeException(e);
        }

    }


    protected void setTracing() {
        PortTraceUtility.setTracing();
        RMITraceUtility.setTracing();
        NIOTraceUtility.setTracing();
        FactoryTraceUtility.setTracing();
        ConsensusTraceUtility.setTracing();
        ThreadDelayed.enablePrint();
        this.trace(true);
    }
}
