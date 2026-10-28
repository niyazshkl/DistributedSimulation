package client;

import util.annotations.Tags;
import util.tags.DistributedTags;
import util.trace.port.consensus.ProposalMade;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
@Tags({DistributedTags.CLIENT_OUT_COUPLER, DistributedTags.RMI, DistributedTags.GIPC, DistributedTags.NIO})
public class OutCoupler implements PropertyChangeListener {

    private final DefaultClient defaultClient;

    public OutCoupler(DefaultClient defaultClient) {
        this.defaultClient = defaultClient;
    }

    @Override
    public void propertyChange(PropertyChangeEvent anEvent) {
        if (!anEvent.getPropertyName().equals("InputString")) return; //this is fine becasue the ipc_mechanims doesn't come from the commandProccessor
        String newCommand = (String) anEvent.getNewValue();
        ProposalMade.newCase(this, this.toString(), -1, anEvent);
        this.defaultClient.send(anEvent); //handles IPC mechanism there
    }
}