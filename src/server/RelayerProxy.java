package server;

import client.DistributedCoupler;
import client.GIPCCoupler;
import util.trace.port.consensus.RemoteProposeRequestReceived;
import util.annotations.Tags;
import util.tags.DistributedTags;

import java.beans.PropertyChangeEvent;
import java.rmi.RemoteException;

@Tags({DistributedTags.SERVER_REMOTE_OBJECT, DistributedTags.RMI, DistributedTags.GIPC})
public class RelayerProxy implements Relayer, GIPCRelayer, DistributedRelayer {
    private DefaultServer defaultServer;

    public RelayerProxy(DefaultServer defaultServer) {
        this.defaultServer = defaultServer;
    }

    @Override
    public void join(String name, DistributedCoupler inCoupler){
        this.defaultServer.addClient(name, inCoupler);
    }

    @Override
    public void join(String name, GIPCCoupler inCoupler) {
        this.defaultServer.addGipcClient(name, inCoupler);
    }

    @Override
    public void send(String sender, PropertyChangeEvent event) {
        RemoteProposeRequestReceived.newCase(this, sender, -1, event);
        this.defaultServer.broadcast(sender,event);
    }

}
