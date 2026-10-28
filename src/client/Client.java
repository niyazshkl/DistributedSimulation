package client;

import assignments.util.inputParameters.SimulationParametersBean;

public interface Client extends SimulationParametersBean {
    void sendCommand(String command);

    void processSimulationCommand(String command);
}
