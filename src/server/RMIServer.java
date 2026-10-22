package server;

import assignments.util.mainArgs.ServerArgsProcessor;

import client.DistributedCoupler;
import shared.Constants;
import util.trace.port.consensus.ProposalLearnedNotificationSent;
import util.annotations.Tags;
import util.tags.DistributedTags;
import util.trace.port.consensus.ProposalMade;
import util.trace.port.rpc.rmi.RMIObjectRegistered;
import util.trace.port.rpc.rmi.RMIRegistryLocated;

import java.beans.PropertyChangeEvent;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;
@Tags({DistributedTags.SERVER_CONFIGURER, DistributedTags.RMI})
public class RMIServer extends Constants implements Server{

    private Map<String, DistributedCoupler> clients;
    private Relayer relayer;
    private String host;
    private int port;

    public RMIServer() {
        super();
        this.clients = new HashMap<>();
        this.relayer = new RelayerProxy(this);
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setPort(int port) {
        this.port = port;
    }
    @Override
    public void launchServer(String[] args){
        String host = ServerArgsProcessor.getRegistryHost(args);
        int port = ServerArgsProcessor.getRegistryPort(args);

        info(host, port);

        RMIServer rmiServer = new RMIServer();

        rmiServer.setHost(host);
        rmiServer.setPort(port);
        rmiServer.registerRemoteObject(args);
    }

    private void info(String host, int port) {
        System.out.println("RMIServer: Registry Host: " + host);
        System.out.println("RMIServer: Registry Port: " + port);
    }

    public void registerRemoteObject(String[] args){
        try {
            Registry rmiRegistry = LocateRegistry.getRegistry(port);

            RMIRegistryLocated.newCase(this, host, port, rmiRegistry);

            DistributedRelayer proxy =
                    (DistributedRelayer) UnicastRemoteObject.exportObject(
                            (DistributedRelayer) relayer, 0
                    );

            rmiRegistry.rebind(
                    Constants.RMI_RELAYER,
                    proxy
            );

            RMIObjectRegistered.newCase(this, Constants.RMI_RELAYER, -1, rmiRegistry);

        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    public void broadcast(String senderName, PropertyChangeEvent event) {

        ProposalMade.newCase(this, senderName, -1, event);
        for (Map.Entry<String, DistributedCoupler> entry : this.clients.entrySet()) {
            if (!entry.getKey().equals(senderName)) {
                ProposalLearnedNotificationSent.newCase(this, entry.getKey(), -1, event);
                try {
                    entry.getValue().receiveChange(event);
                } catch (RemoteException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public void addClient(String name, DistributedCoupler inCoupler){
        this.clients.put(name, inCoupler); //overwrites previous incoupler of a client
    }

}
