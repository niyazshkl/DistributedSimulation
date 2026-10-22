package client;

import util.annotations.Tags;
import util.tags.DistributedTags;

import java.beans.PropertyChangeEvent;
import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;

@Tags({DistributedTags.CLIENT_REMOTE_INTERFACE, DistributedTags.RMI})
public interface DistributedCoupler extends Remote, Serializable {
    void receiveChange(PropertyChangeEvent anEvent) throws RemoteException;
}
