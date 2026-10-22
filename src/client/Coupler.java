package client;

import client.modifiedStarter.DistributedCommandProcessor;

public interface Coupler {
    void setObservingProcessor(DistributedCommandProcessor observingProcessor);
}
