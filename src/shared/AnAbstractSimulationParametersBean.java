package shared;

import assignments.util.inputParameters.SimulationParametersBean;
import util.interactiveMethodInvocation.ConsensusAlgorithm;
import util.interactiveMethodInvocation.IPCMechanism;
import util.trace.port.consensus.ProposalMade;
import util.trace.port.consensus.ProposedStateSet;

public abstract class AnAbstractSimulationParametersBean implements SimulationParametersBean {
    protected boolean localProcessingOnly = false;
    protected boolean broadcastBroadcastMode = false;
    protected boolean broadcastIPCMechanism = false;
    protected boolean broadcastMetaState = false;
    protected boolean waitForBroadcastConsensus = false;
    protected boolean waitForIPCMechanismConsensus = false;
    protected ConsensusAlgorithm consensusAlgorithm;
    protected boolean rejectMetaStateChange;
    protected Boolean atomicBroadcast;
    protected int delay;
    protected boolean trace;
    protected IPCMechanism ipcMechanism;

    public AnAbstractSimulationParametersBean() {
        this.consensusAlgorithm = ConsensusAlgorithm.CENTRALIZED_ASYNCHRONOUS;
        this.rejectMetaStateChange = false;
        this.atomicBroadcast = false;
        this.delay = 0;
        this.trace = false;
        this.ipcMechanism = IPCMechanism.NIO;
    }

    public synchronized Boolean isAtomicBroadcast() {
        return this.atomicBroadcast;
    }

    public boolean isLocalProcessingOnly() {
        return this.localProcessingOnly;
    }

    public void setLocalProcessingOnly(boolean localProcessingOnly) {
        this.localProcessingOnly = localProcessingOnly;
    }

    public boolean isBroadcastBroadcastMode() {
        return this.broadcastBroadcastMode;
    }

    public void setBroadcastBroadcastMode(boolean broadcastBroadcastMode) {
        this.broadcastBroadcastMode = broadcastBroadcastMode;
    }

    public boolean isBroadcastIPCMechanism() {
        return this.broadcastIPCMechanism;
    }

    public void setBroadcastIPCMechanism(boolean broadcastIPCMechanism) {
        this.broadcastIPCMechanism = broadcastIPCMechanism;
    }

    public boolean isWaitForBroadcastConsensus() {
        return this.waitForBroadcastConsensus;
    }

    public void setWaitForBroadcastConsensus(boolean waitForBroadcastConsensus) {
        this.waitForBroadcastConsensus = waitForBroadcastConsensus;
    }

    public boolean isWaitForIPCMechanismConsensus() {
        return this.waitForIPCMechanismConsensus;
    }

    public void setWaitForIPCMechanismConsensus(boolean waitForIPCMechanismConsensus) {
        this.waitForIPCMechanismConsensus = waitForIPCMechanismConsensus;
    }

    public ConsensusAlgorithm getConsensusAlgorithm() {
        return this.consensusAlgorithm;
    }

    public void setConsensusAlgorithm(ConsensusAlgorithm consensusAlgorithm) {
        this.consensusAlgorithm = consensusAlgorithm;
    }

    public synchronized void setAtomicBroadcast(Boolean newValue) {
        ProposedStateSet.newCase(this, "broadcast_mode", -1.0F, newValue);
        this.atomicBroadcast = newValue;
    }

    public void atomicBroadcast(boolean newValue) {
        ProposalMade.newCase(this, "broadcast_mode", -1.0F, newValue);
        this.setAtomicBroadcast(newValue);
    }

    public synchronized void setIPCMechanism(IPCMechanism newValue) {
        ProposedStateSet.newCase(this, "ipc_mechanism", -1.0F, newValue);
        this.ipcMechanism = newValue;
    }

    public synchronized IPCMechanism getIPCMechanism() {
        return this.ipcMechanism;
    }

    public void ipcMechanism(IPCMechanism newValue) {
        ProposalMade.newCase(this, "ipc_mechanism", -1.0F, newValue);
        this.setIPCMechanism(newValue);
    }

    public void localProcessingOnly(boolean newValue) {
        this.setLocalProcessingOnly(newValue);
    }

    public void broadcastBroadcastMode(boolean newValue) {
        this.setBroadcastBroadcastMode(newValue);
    }

    public void broadcastMetaState(boolean newValue) {
        this.setBroadcastMetaState(newValue);
    }

    public void waitForBroadcastConsensus(boolean newValue) {
        this.setWaitForBroadcastConsensus(newValue);
    }

    public void broadcastIPCMechanism(boolean newValue) {
        this.setBroadcastIPCMechanism(newValue);
    }

    public boolean isBroadcastMetaState() {
        return this.broadcastMetaState;
    }

    public void setBroadcastMetaState(boolean broadcastMetaState) {
        this.broadcastMetaState = broadcastMetaState;
        this.broadcastBroadcastMode(broadcastMetaState);
        this.broadcastIPCMechanism(broadcastMetaState);
    }

    public void waitForIPCMechanismConsensus(boolean newValue) {
        this.setWaitForIPCMechanismConsensus(newValue);
    }

    public void consensusAlgorithm(ConsensusAlgorithm newValue) {
        this.setConsensusAlgorithm(newValue);
    }

    public void simulationCommand(String aCommand) {
        ProposalMade.newCase(this, "command", -1.0F, aCommand);
    }

    public void delaySends(int aMillisecondDelay) {
        this.setDelay(aMillisecondDelay);
    }

    public int getDelay() {
        return this.delay;
    }

    public void setDelay(int delay) {
        this.delay = delay;
    }

    public boolean isRejectMetaStateChange() {
        return this.rejectMetaStateChange;
    }

    public void setRejectMetaStateChange(boolean rejectMetaStateChange) {
        this.rejectMetaStateChange = rejectMetaStateChange;
    }

    public void rejectMetaStateChange(boolean newValue) {
        this.setRejectMetaStateChange(newValue);
    }

    public void trace(boolean newValue) {
        this.setTrace(newValue);
    }

    public boolean isTrace() {
        return this.trace;
    }

    public void setTrace(boolean trace) {
        this.trace = trace;
    }
}

