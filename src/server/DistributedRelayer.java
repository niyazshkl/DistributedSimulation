package server;

import java.beans.PropertyChangeEvent;
import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface DistributedRelayer extends Remote, Serializable {
    void join(String clientName, DistributedPropertyChangeListener propertyChangeListener) throws RemoteException;
    void broadcast(String clientName, PropertyChangeEvent event) throws RemoteException;
}
