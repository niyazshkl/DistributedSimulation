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

            //Server proxy create -> Add to the registry to be later reetrieved

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
