package server;

import assignments.util.inputParameters.AnAbstractSimulationParametersBean;
import assignments.util.mainArgs.RegistryArgsProcessor;
import assignments.util.mainArgs.ServerArgsProcessor;

import client.DistributedCoupler;
import shared.traces.ProposalLearnedNotificationSent;
import util.annotations.Tags;
import util.tags.DistributedTags;
import util.trace.factories.FactoryTraceUtility;
import util.trace.misc.ThreadDelayed;
import util.trace.port.PortTraceUtility;
import util.trace.port.consensus.ConsensusTraceUtility;
import util.trace.port.consensus.ProposalMade;
import util.trace.port.nio.NIOTraceUtility;
import util.trace.port.rpc.rmi.RMIObjectRegistered;
import util.trace.port.rpc.rmi.RMIRegistryLocated;
import util.trace.port.rpc.rmi.RMITraceUtility;

import java.beans.PropertyChangeEvent;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;
@Tags({DistributedTags.SERVER_CONFIGURER, DistributedTags.RMI})
public class RMIServer extends AnAbstractSimulationParametersBean {
    public Map<String, DistributedCoupler> getClients() {
        return clients;
    }

    private Map<String, DistributedCoupler> clients;
    private Relayer relayer;

    public RMIServer() {
        this.clients = new HashMap<>();
        this.relayer = new RelayerProxy(this);
    }

    public static void main(String[] args){
       RMIServer rmiServer = new RMIServer();
       rmiServer.setTracing();
       rmiServer.startServer(args);
    }

    public void startServer(String[] args){
        try {

            int registryPort = ServerArgsProcessor.getRegistryPort(args);
            String host = ServerArgsProcessor.getRegistryHost(args);

            Registry rmiRegistry = LocateRegistry.getRegistry(RegistryArgsProcessor.getRegistryPort(args));

            RMIRegistryLocated.newCase(this, host, registryPort, rmiRegistry);

            //Server proxy create -> Add to the registry to be later reetrieved

            DistributedRelayer stub =
                    (DistributedRelayer) UnicastRemoteObject.exportObject(
                            (DistributedRelayer) relayer, 0
                    );

            rmiRegistry.rebind(
                    "RELAYER",
                    stub
            );
            RMIObjectRegistered.newCase(this,"RELAYER", -1, rmiRegistry);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    public void broadcast(String senderName, PropertyChangeEvent event) {

        ProposalMade.newCase(this, senderName, -1, event);
        for (Map.Entry<String, DistributedCoupler> entry : this.getClients().entrySet()) {
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
    protected void setTracing() {
        PortTraceUtility.setTracing();
        RMITraceUtility.setTracing();
        NIOTraceUtility.setTracing();
        FactoryTraceUtility.setTracing();
        ConsensusTraceUtility.setTracing();
        ThreadDelayed.enablePrint();
        this.trace(true);
    }
}
