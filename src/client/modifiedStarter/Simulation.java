package client.modifiedStarter;

import assignments.util.inputParameters.SimulationParametersBean;
import assignments.util.inputParameters.SimulationParametersListener;

public interface Simulation extends SimulationParametersListener {
    void atomicBroadcast(boolean isBroadcast);
}
