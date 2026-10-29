package client;

import assignments.util.inputParameters.SimulationParametersBean;

import java.beans.PropertyChangeEvent;

public interface Client extends SimulationParametersBean {
    void handleProposalAcceptRequest(PropertyChangeEvent anEvent);
}
