package server;

import java.beans.PropertyChangeEvent;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;

public class ADistributedRelayer implements Relayer, DistributedRelayer {

    private Map<String, DistributedPropertyChangeListener> outCouplers = new HashMap<>();

    @Override
    public void join(String clientName, DistributedPropertyChangeListener propertyChangeListener){
        if (outCouplers.containsKey(clientName)) return ; //no overriding!
        this.outCouplers.put(clientName, propertyChangeListener);
        System.out.println("ADDED NEW OUTCOPLER: " + outCouplers.get(clientName));
    }

    @Override
    public void broadcast(String clientName, PropertyChangeEvent event) {
        for (Map.Entry<String, DistributedPropertyChangeListener> entry : outCouplers.entrySet()) {
            if (!entry.getKey().equals(clientName)) {
                try {
                    System.out.println("HI in broadact gonna broast");
                    entry.getValue().propertyChange(event); // send updates to all others
                    System.out.println("HI in broadact SENT broast");
                } catch (RemoteException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

}
