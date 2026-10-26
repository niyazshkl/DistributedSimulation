package client;

import client.modifiedStarter.DistributedCommandProcessor;
import util.annotations.Tags;
import util.interactiveMethodInvocation.IPCMechanism;
import util.tags.DistributedTags;
import util.trace.port.consensus.ProposalLearnedNotificationReceived;
import util.trace.trickOrTreat.LocalCommandObserved;

import java.beans.PropertyChangeEvent;
import java.rmi.RemoteException;

@Tags({DistributedTags.CLIENT_REMOTE_OBJECT, DistributedTags.RMI, DistributedTags.GIPC})
public class InCoupler implements Coupler, GIPCCoupler, DistributedCoupler { //client proxy will be called

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
        //have to account for either the IPC is RMI/GIPC and if the propertyName is ipc_mehanism itself.
        String propertyName = anEvent.getPropertyName();
        if (propertyName.equals("InputString")){
        String newValue = (String) anEvent.getNewValue();
        LocalCommandObserved.newCase(this, newValue);
        try {
            observingProcessor.processCommand(newValue);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
        } else if (propertyName.equals("ipc_mechanism")){
            IPCMechanism newValue =
                    (IPCMechanism) anEvent.getNewValue();
            this.client.setIPCMechanism(newValue);
        }
        //what if the received thing is ipc_mechanism? we need to set it here also
    }



}