package server;

import client.DistributedCoupler;
import client.GIPCCoupler;

import java.beans.PropertyChangeEvent;
import java.nio.channels.SocketChannel;

public interface Server {
    void broadcast(String senderName, PropertyChangeEvent event);

    void addClient(String name, DistributedCoupler inCoupler);

    void addGipcClient(String name, GIPCCoupler inCoupler);

    void addClientNioSocketChannel(SocketChannel clientSocketChannel);

    void nioBroadcast(String client, String command);
}
