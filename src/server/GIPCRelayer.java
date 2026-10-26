package server;

import client.DistributedCoupler;

import java.beans.PropertyChangeEvent;

public interface GIPCRelayer {
    void join(String name, DistributedCoupler inCoupler);
    void send(String sender, PropertyChangeEvent event);
}
