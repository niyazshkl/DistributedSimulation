package client;

import java.beans.PropertyChangeEvent;
import java.io.Serializable;

public interface GIPCCoupler extends Serializable {
    void receiveChange(PropertyChangeEvent anEvent);
}
