package client;

import util.annotations.Tags;
import util.tags.DistributedTags;
import util.trace.port.consensus.ProposalMade;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
@Tags({DistributedTags.CLIENT_OUT_COUPLER, DistributedTags.RMI})
public class OutCoupler implements PropertyChangeListener {

    private final RMIClient rmiClient;

    public OutCoupler(RMIClient rmiClient) {
        this.rmiClient = rmiClient;
    }

    @Override
    public void propertyChange(PropertyChangeEvent anEvent) { //update sent from relayer!
        if (!anEvent.getPropertyName().equals("InputString")) return;
        String newCommand = (String) anEvent.getNewValue();
        ProposalMade.newCase(this, this.toString(), -1, anEvent);
        this.rmiClient.send(anEvent);
    }
}