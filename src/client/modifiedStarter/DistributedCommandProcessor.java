package client.modifiedStarter;

import java.beans.PropertyChangeListener;
import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface DistributedCommandProcessor extends Remote, Serializable {
    void addPropertyChangeListener(PropertyChangeListener var1) throws RemoteException;
    void processCommand(String var1) throws RemoteException;
}
