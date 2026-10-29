package server;

import client.GIPCCoupler;
import util.annotations.Tags;
import util.tags.DistributedTags;

import java.beans.PropertyChangeEvent;
@Tags({DistributedTags.SERVER_REMOTE_INTERFACE, DistributedTags.GIPC})
public interface GIPCRelayer {
    void join(String name, GIPCCoupler inCoupler);
    void receive(String sender, PropertyChangeEvent event);
}
