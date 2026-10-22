package server;

import client.DistributedCoupler;
import util.annotations.Tags;
import util.tags.DistributedTags;

import java.beans.PropertyChangeEvent;
import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;
@Tags({DistributedTags.SERVER_REMOTE_INTERFACE, DistributedTags.RMI})
public interface DistributedRelayer extends Remote, Serializable {
    void join(String name, DistributedCoupler inCoupler) throws RemoteException;
    void send(String sender, PropertyChangeEvent event) throws RemoteException;
}
