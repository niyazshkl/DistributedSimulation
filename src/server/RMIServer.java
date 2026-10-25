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
    private String registryHost;
    private int registryPort;

    public RMIServer(String registryHost, int registryPort) {
        super(); //set tracing is done.
        this.registryHost = registryHost;
        this.registryPort = registryPort;
        this.clients = new HashMap<>();
        this.relayer = new RelayerProxy(this);
    }

    public void setRegistryHost(String registryHost) {
        this.registryHost = registryHost;
    }

    public void setRegistryPort(int registryPort) {
        this.registryPort = registryPort;
    }

    public static void launchServer(String[] args){
        String registryHost = ServerArgsProcessor.getRegistryHost(args);
        int registryPort = ServerArgsProcessor.getRegistryPort(args);
        RMIServer rmiServer = new RMIServer(registryHost, registryPort);
        rmiServer.launchServer();
    }

    public void launchServer(){
        this.info();
        this.boilerPlate();
        this.registerRemoteObject();
    }

    private void info() {
        System.out.println("RMIServer: Registry Host: " + registryHost);
        System.out.println("RMIServer: Registry Port: " + registryPort);
    }

    public void registerRemoteObject(){
        try {
            Registry rmiRegistry = LocateRegistry.getRegistry(registryPort);

            RMIRegistryLocated.newCase(this, registryHost, registryPort, rmiRegistry);

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
