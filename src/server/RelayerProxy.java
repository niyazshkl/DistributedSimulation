package server;

import client.DistributedCoupler;
import client.GIPCCoupler;
import util.trace.port.consensus.RemoteProposeRequestReceived;
import util.annotations.Tags;
import util.tags.DistributedTags;

import java.beans.PropertyChangeEvent;

@Tags({DistributedTags.SERVER_REMOTE_OBJECT, DistributedTags.RMI, DistributedTags.GIPC})
public class RelayerProxy implements Relayer, GIPCRelayer, DistributedRelayer {
    private Server server;

    public RelayerProxy(Server server) {
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
    public void send(String sender, PropertyChangeEvent event) {
        RemoteProposeRequestReceived.newCase(this, sender, -1, event);
        this.server.broadcast(sender,event);
    }

}
