package server;

import assignments.util.inputParameters.AnAbstractSimulationParametersBean;
import assignments.util.mainArgs.RegistryArgsProcessor;
import assignments.util.mainArgs.ServerArgsProcessor;

import util.trace.factories.FactoryTraceUtility;
import util.trace.misc.ThreadDelayed;
import util.trace.port.PortTraceUtility;
import util.trace.port.consensus.ConsensusTraceUtility;
import util.trace.port.nio.NIOTraceUtility;
import util.trace.port.rpc.rmi.RMIObjectRegistered;
import util.trace.port.rpc.rmi.RMIRegistryCreated;
import util.trace.port.rpc.rmi.RMIRegistryLocated;
import util.trace.port.rpc.rmi.RMITraceUtility;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class RMIServer extends AnAbstractSimulationParametersBean {
    public static void main(String[] args){
        new RMIServer().createRegistry(args);
    }
    public void createRegistry(String[] args){
        try {
            setTracing();
            int registryPort = ServerArgsProcessor.getRegistryPort(args);
            String host = ServerArgsProcessor.getRegistryHost(args);
            Registry rmiRegistry = LocateRegistry.getRegistry(RegistryArgsProcessor.getRegistryPort(args));
            RMIRegistryLocated.newCase(this, host, registryPort, rmiRegistry);
            String objectName = "RELAYER";



            Relayer relayer = new ADistributedRelayer();

            UnicastRemoteObject.exportObject((DistributedRelayer) relayer,0); //create proxy -> same object instead of a copy sent


            RMIObjectRegistered.newCase(this, objectName, relayer, rmiRegistry);
            rmiRegistry.rebind(objectName, (DistributedRelayer) relayer);

        } catch (RemoteException e) {
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
