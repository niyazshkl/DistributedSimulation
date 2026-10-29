package server;

import client.DistributedCoupler;
import client.GIPCCoupler;
import consensus.ProposalFeedbackKind;
import util.annotations.Tags;
import util.tags.DistributedTags;
import util.trace.port.consensus.ProposalAcceptRequestReceived;
import util.trace.port.consensus.RemoteProposeRequestReceived;

import java.beans.PropertyChangeEvent;
import java.rmi.RemoteException;

@Tags({DistributedTags.SERVER_REMOTE_OBJECT, DistributedTags.RMI, DistributedTags.GIPC, DistributedTags.ATOMIC_BROADCAST, DistributedTags.TWO_PHASE_COMMIT})
public class DefaultRelayer implements Relayer, GIPCRelayer, DistributedRelayer {
    private Server server;

    public DefaultRelayer(Server server) {
        this.server = server;
    }

    @Override
    public void join(String name, DistributedCoupler inCoupler){
        this.server.addClient(name, inCoupler);
    }

    @Override
    public void join(String name, GIPCCoupler inCoupler) {
        this.server.addGipcClient(name, inCoupler);
    }

    @Override
    public void receive(String sender, PropertyChangeEvent event) {
        RemoteProposeRequestReceived.newCase(this, sender, -1, event);
        this.server.broadcast(sender,event);
    }

    @Override
    public void receiveProposalAcceptResponse(String sender, PropertyChangeEvent anEvent, ProposalFeedbackKind response) throws RemoteException {
        ProposalAcceptRequestReceived.newCase(this, "ProposalAcceptResponse", -1, anEvent);
        this.server.handleProposalAcceptRequestResponse(sender, anEvent, response);
    }

}
