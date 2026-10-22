package client;

import assignments.util.inputParameters.AnAbstractSimulationParametersBean;
import assignments.util.inputParameters.SimulationParametersListener;
import assignments.util.mainArgs.ClientArgsProcessor;
import shared.AHalloweenCommandProcessor;
import shared.DistributedCommandProcessor;
import graphics.HalloweenSimulation;
import main.BeauAndersonFinalProject;
import stringProcessors.HalloweenCommandProcessor;
import util.misc.ThreadSupport;
import util.trace.Tracer;
import util.trace.factories.FactoryTraceUtility;
import util.trace.misc.ThreadDelayed;
import util.trace.port.PerformanceExperimentEnded;
import util.trace.port.PerformanceExperimentStarted;
import util.trace.port.PortTraceUtility;
import util.trace.port.consensus.ConsensusTraceUtility;
import util.trace.port.nio.NIOTraceUtility;
import util.trace.port.rpc.rmi.RMITraceUtility;

import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



public class ASimulation extends AnAbstractSimulationParametersBean implements SimulationParametersListener {
    private final String clientName;
    HalloweenCommandProcessor commandProcessor;
    HalloweenCommandProcessor commandProcessor2;
    protected int NUM_EXPERIMENT_COMMANDS = 500;
    public static final String EXPERIMENT_COMMAND_1 = "move 1 -1";
    public static final String EXPERIMENT_COMMAND_2 = "undo";
    protected PropertyChangeListener outCoupler;
    protected DistributedCoupler inCoupler;

    public DistributedCoupler getInCoupler() {
        return inCoupler;
    }

    Map<String, List<Integer>> simulationValues = new HashMap<>();

    public static boolean isFirst = true;

    public ASimulation(String clientName) {
        this.clientName = clientName;
        this.init();
    }
    public HalloweenCommandProcessor createSimulation(String titlePrefix, int simulationX, int simulationY, int simulationWidth, int simulationHeight, int commandX, int commandY) {
        HalloweenSimulation aNewHalloweenSim = BeauAndersonFinalProject.createAndDisplaySimulationWithoutCommandProcessor(simulationX, simulationY, simulationWidth, simulationHeight, titlePrefix);
        HalloweenCommandProcessor commandLine = new AHalloweenCommandProcessor();
        BeauAndersonFinalProject.bindAndDisplayCommandProcessor(aNewHalloweenSim, commandLine, titlePrefix, commandX, commandY, simulationWidth);
        return commandLine;
    }

    protected HalloweenCommandProcessor createSimulation1(String aPrefix) {
        return createSimulation(aPrefix, 0, 0, 400, 765, 0, 0);
    }

    protected HalloweenCommandProcessor createSimulation2(String aPrefix) {
        return createSimulation(aPrefix, 400, 0, 400, 765, 400, 0);
    }

    void processArgs(String[] args) {
        System.out.println("Registry host:" + assignments.util.mainArgs.ClientArgsProcessor.getRegistryHost(args));
        System.out.println("Registry port:" + assignments.util.mainArgs.ClientArgsProcessor.getRegistryPort(args));
        System.out.println("Server host:" + assignments.util.mainArgs.ClientArgsProcessor.getServerHost(args));
        System.out.println("Headless:" + assignments.util.mainArgs.ClientArgsProcessor.getHeadless(args));
        System.out.println("Client name:" + assignments.util.mainArgs.ClientArgsProcessor.getClientName(args));
        System.setProperty("java.awt.headless", ClientArgsProcessor.getHeadless(args));
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

    protected void init() {

        this.setTracing();
//        this.processArgs(args);
        if (isFirst) {
            this.commandProcessor = this.createSimulation1("1:");
            isFirst = false;
        } else {
            this.commandProcessor = this.createSimulation2("2:");
        }

        this.inCoupler = new InCoupler( (DistributedCommandProcessor) this.commandProcessor, clientName); //waits to get from outCoupler

        //this.commandProcessor.addPropertyChangeListener();

    }

    public void setOutCoupler(PropertyChangeListener outCoupler){
        this.outCoupler = outCoupler;
        this.commandProcessor.addPropertyChangeListener(this.outCoupler);
    }

//    public void start(String[] args) {
//        this.init(args);
//        SimulationParametersControllerFactory.getSingleton().addSimulationParameterListener(this);
//        SimulationParametersControllerFactory.getSingleton().processCommands();
//    }

    public void quit(int aCode) {
        System.exit(aCode);
    }

    public void simulationCommand(String aCommand) {
        long aDelay = (long)this.getDelay();
        if (aDelay > 0L) {
            ThreadSupport.sleep(aDelay);
        }

        this.commandProcessor.setInputString(aCommand);
    }

    public void trace(boolean newValue) {
        super.trace(newValue);
        Tracer.showInfo(this.isTrace());
    }

//    public void localProcessingOnly(boolean newValue) {
//        super.localProcessingOnly(newValue);
//        if (this.isLocalProcessingOnly()) {
//            this.commandProcessor.removePropertyChangeListener(this.simulation2Coupler);
//            this.commandProcessor2.removePropertyChangeListener(this.outCoupler);
//        } else {
//            this.commandProcessor.addPropertyChangeListener(this.simulation2Coupler);
//            this.commandProcessor2.addPropertyChangeListener(this.outCoupler);
//        }
//
//    }

    public void atomicBroadcast(boolean newValue) {
        super.atomicBroadcast(newValue);
        this.commandProcessor.setConnectedToSimulation(!this.isAtomicBroadcast());
        this.commandProcessor2.setConnectedToSimulation(!this.isAtomicBroadcast());
    }

    public void experimentInput() {
        long aStartTime = System.currentTimeMillis();
        PerformanceExperimentStarted.newCase(this, aStartTime, this.NUM_EXPERIMENT_COMMANDS);
        boolean anOldValue = this.isTrace();
        this.trace(false);

        for(int i = 0; i < this.NUM_EXPERIMENT_COMMANDS; ++i) {
            this.commandProcessor.setInputString("move 1 -1");
            this.commandProcessor.setInputString("undo");
        }

        this.trace(anOldValue);
        long anEndTime = System.currentTimeMillis();
        PerformanceExperimentEnded.newCase(this, aStartTime, anEndTime, anEndTime - aStartTime, this.NUM_EXPERIMENT_COMMANDS);
    }

    public void delaySends(int aMillisecondDelay) {
        super.delaySends(aMillisecondDelay);
    }
}
