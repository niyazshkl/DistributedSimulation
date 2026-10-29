package server;

import client.DistributedCoupler;
import client.GIPCCoupler;
import consensus.ProposalFeedbackKind;

import java.beans.PropertyChangeEvent;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public interface Server {
    void broadcast(String senderName, PropertyChangeEvent event);

    void addClient(String name, DistributedCoupler inCoupler);

    void addGipcClient(String name, GIPCCoupler inCoupler);

    void addClientNioSocketChannel(SocketChannel clientSocketChannel);

    void handleProposalAcceptRequestResponse(String sender, PropertyChangeEvent anEvent, ProposalFeedbackKind response);

    void nioBroadcast(SocketAddress client, ByteBuffer command);
}
