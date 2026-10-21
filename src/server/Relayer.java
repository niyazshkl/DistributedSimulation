package server;

import java.beans.PropertyChangeEvent;

public interface Relayer {
    void join(String clientName, DistributedPropertyChangeListener propertyChangeListener);

    void broadcast(String clientName, PropertyChangeEvent event);
}
