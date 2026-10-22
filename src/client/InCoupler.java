package client;

import client.modifiedStarter.DistributedCommandProcessor;
import util.annotations.Tags;
import util.tags.DistributedTags;
import util.trace.port.consensus.ProposalLearnedNotificationReceived;
import util.trace.trickOrTreat.LocalCommandObserved;

import java.beans.PropertyChangeEvent;
import java.rmi.RemoteException;

@Tags({DistributedTags.CLIENT_REMOTE_OBJECT, DistributedTags.RMI})
public class InCoupler implements Coupler, DistributedCoupler { //client proxy will be called

    DistributedCommandProcessor observingProcessor;

    Client client;
    public InCoupler(){}
    public InCoupler(Client client) {
        this.client = client;
    }
    public void setObservingProcessor(DistributedCommandProcessor observingProcessor) {
        this.observingProcessor = observingProcessor;
    }

    @Override
    public void receiveChange(PropertyChangeEvent anEvent) { //update sent from relayer!

        ProposalLearnedNotificationReceived.newCase(this, "anEvent", -1, anEvent);

        if (!anEvent.getPropertyName().equals("InputString")) return;
        String newCommand = (String) anEvent.getNewValue();
        LocalCommandObserved.newCase(this, newCommand);
//		System.out.println("Received command:" + newCommand);
        try {
            observingProcessor.processCommand(newCommand);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }



}