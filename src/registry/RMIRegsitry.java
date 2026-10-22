package registry;

import assignments.util.inputParameters.AnAbstractSimulationParametersBean;
import assignments.util.mainArgs.RegistryArgsProcessor;
import coupledsims.AStandAloneTwoCoupledHalloweenSimulations;
import graphics.AHalloweenSimulation;
import stringProcessors.AHalloweenCommandProcessor;
import util.annotations.Tags;
import util.tags.DistributedTags;
import util.trace.factories.FactoryTraceUtility;
import util.trace.misc.ThreadDelayed;
import util.trace.port.PortTraceUtility;
import util.trace.port.consensus.ConsensusTraceUtility;
import util.trace.port.nio.NIOTraceUtility;
import util.trace.port.rpc.rmi.RMIRegistryCreated;
import util.trace.port.rpc.rmi.RMITraceUtility;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.Scanner;
@Tags({DistributedTags.REGISTRY, DistributedTags.RMI})
public class RMIRegsitry extends AnAbstractSimulationParametersBean {
    public static void main(String[] args){
        new RMIRegsitry().createRegistry(args);
    }
    public void createRegistry(String[] args){
        try {
            setTracing();
            int registryPort = RegistryArgsProcessor.getRegistryPort(args);
            RMIRegistryCreated.newCase(this, registryPort);
            LocateRegistry.createRegistry(RegistryArgsProcessor.getRegistryPort(args));
            Scanner scanner = new Scanner(System.in);
            scanner.nextLine();
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
