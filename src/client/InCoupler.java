package client;

import shared.DistributedCommandProcessor;
import shared.traces.ProposalLearnedNotificationReceived;
import util.annotations.Tags;
import util.tags.DistributedTags;
import util.trace.trickOrTreat.LocalCommandObserved;

import java.beans.PropertyChangeEvent;
import java.rmi.RemoteException;
@Tags({DistributedTags.CLIENT_REMOTE_OBJECT, DistributedTags.RMI})
public class InCoupler implements Coupler, DistributedCoupler { //client proxy will be called

    DistributedCommandProcessor observingSimulation;

    String clientName;

    public InCoupler(DistributedCommandProcessor anObservingSimulation, String clientName) {
        observingSimulation = anObservingSimulation;
        this.clientName = clientName;
    }

    @Override
    public void receiveChange(PropertyChangeEvent anEvent) { //update sent from relayer!

        ProposalLearnedNotificationReceived.newCase(this, clientName, -1, anEvent);

        if (!anEvent.getPropertyName().equals("InputString")) return;
        String newCommand = (String) anEvent.getNewValue();
        LocalCommandObserved.newCase(this, newCommand);
//		System.out.println("Received command:" + newCommand);
        try {
            observingSimulation.processCommand(newCommand);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }



}