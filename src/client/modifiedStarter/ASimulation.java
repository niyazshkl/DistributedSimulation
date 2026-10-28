package client.modifiedStarter;

import assignments.util.inputParameters.AnAbstractSimulationParametersBean;
import client.Client;
import client.Coupler;
import graphics.HalloweenSimulation;
import main.BeauAndersonFinalProject;
import stringProcessors.HalloweenCommandProcessor;
import util.misc.ThreadSupport;
import util.trace.Tracer;
import util.trace.port.PerformanceExperimentEnded;
import util.trace.port.PerformanceExperimentStarted;
import util.trace.trickOrTreat.LocalCommandObserved;

import java.beans.PropertyChangeListener;


public class ASimulation extends AnAbstractSimulationParametersBean implements Simulation {
    private final Client client;
    HalloweenCommandProcessor commandProcessor;
    protected int NUM_EXPERIMENT_COMMANDS = 500;
    protected PropertyChangeListener outCoupler;
    protected Coupler inCoupler;

    public void setInCoupler(Coupler inCoupler) {
        this.inCoupler = inCoupler;
        this.inCoupler.setObservingProcessor((DistributedCommandProcessor) this.commandProcessor); //waits for outCoupler notify
    }

    public Coupler getInCoupler() {
        return inCoupler;
    }

    public static boolean isFirst = true;

    public ASimulation(Client client) {
        this.client = client;
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

    protected void init() {

        if (isFirst) {
            this.commandProcessor = this.createSimulation1("1:");
            isFirst = false;
        } else {
            this.commandProcessor = this.createSimulation2("2:");
        }
    }

    public void setOutCoupler(PropertyChangeListener outCoupler){
        this.outCoupler = outCoupler;
        this.commandProcessor.addPropertyChangeListener(this.outCoupler);
    }
    public void setInCoupler(PropertyChangeListener outCoupler){
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
    @Override
    public void processSimulationCommand(String command){
        this.commandProcessor.processCommand(command);
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
