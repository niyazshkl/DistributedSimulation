package client.modifiedStarter;

import assignments.util.inputParameters.SimulationParametersListener;

public interface Simulation extends SimulationParametersListener {
    void processSimulationCommand(String command);
}
