package server;

import client.DistributedCoupler;
import util.trace.port.consensus.RemoteProposeRequestReceived;
import util.annotations.Tags;
import util.tags.DistributedTags;

import java.beans.PropertyChangeEvent;
import java.rmi.RemoteException;

@Tags({DistributedTags.SERVER_REMOTE_OBJECT, DistributedTags.RMI})
public class RelayerProxy implements Relayer, DistributedRelayer {
    private RMIServer rmiServer;

    public RelayerProxy(RMIServer rmiServer) {
        this.rmiServer = rmiServer;
    }

    @Override
    public void join(String name, DistributedCoupler inCoupler){
        this.rmiServer.addClient(name, inCoupler);
    }

    @Override
    public void send(String sender, PropertyChangeEvent event) throws RemoteException {
        RemoteProposeRequestReceived.newCase(this, sender, -1, event);
        this.rmiServer.broadcast(sender,event);
    }

}
