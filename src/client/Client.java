package client;

import assignments.util.mainArgs.ServerArgsProcessor;
import coupledsims.ASimulationCoupler;
import main.BeauAndersonFinalProject;
import server.DistributedRelayer;
import stringProcessors.HalloweenCommandProcessor;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.rmi.RemoteException;

public class Client implements PropertyChangeListener {
    private String name;
    private DistributedCoupler outCoupler;

    public void setRelayer(DistributedRelayer relayer) {
        this.relayer = relayer;
    }

    DistributedRelayer relayer;

    public void setName(String name) {
        this.name = name;
    }

    private InCoupler inCoupler;
    private HalloweenCommandProcessor commandProcessor;
    private int simulationX = 0;
    private int simulationY = 0;
    private int simulationWidth = 400;
    private int simulationHeight = 765;
    private int commandX = 0;
    private int commandY = 0;

    public Client(String name, DistributedCoupler outCoupler) {
        this.name = name;
        this.outCoupler = outCoupler;
    }

    public void buildSimulation() { //init()
        this.commandProcessor = BeauAndersonFinalProject.createSimulation(name,simulationX, simulationY, simulationWidth, simulationHeight, commandX, commandY);
        this.inCoupler = new InCoupler(commandProcessor);
        //inc ouptler looks at the command processor -> if notified (which only happens if not by setInptuStrping) -> Notifies outcouple -> it has copy to proxy & incouple which calls broacast which will call all
        //inc ouptler looks at the command processor -> if notified (which only happens if not by setInptuStrping) -> Notifies CLIENT.class -> it has copy to proxy broacast which will call all outcouples which will call their incouple sto update.
        this.commandProcessor.addPropertyChangeListener((PropertyChangeListener) this.outCoupler);
        ServerArgsProcessor
//        this.commandProcessor.addPropertyChangeListener((PropertyChangeListener) this.inCoupler);
        try {
            this.outCoupler.addPropertyChangeListener(inCoupler); //in coupler notified by outCoupler!
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    public void setSimulationX(int simulationX) {
        this.simulationX = simulationX;
    }

    public void setSimulationY(int simulationY) {
        this.simulationY = simulationY;
    }

    public void setSimulationWidth(int simulationWidth) {
        this.simulationWidth = simulationWidth;
    }

    public void setSimulationHeight(int simulationHeight) {
        this.simulationHeight = simulationHeight;
    }

    public void setCommandX(int commandX) {
        this.commandX = commandX;
    }

    public void setCommandY(int commandY) {
        this.commandY = commandY;
    }

    public String getName() {
        return name;
    }

    public DistributedPropertyChangeListener getOutCoupler() {
        return outCoupler;
    }

    /**
     * This method gets called when a bound property is changed.
     *
     * @param evt A PropertyChangeEvent object describing the event source
     *            and the property that has changed.
     */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        try {
            this.relayer.broadcast(name, evt);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }
}
