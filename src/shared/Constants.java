package shared;

import assignments.util.inputParameters.AnAbstractSimulationParametersBean;
import coupledsims.AStandAloneTwoCoupledHalloweenSimulations;
import inputport.ConnectionListener;
import inputport.ConnectionType;
import util.interactiveMethodInvocation.SimulationParametersControllerFactory;
import util.trace.factories.FactoryTraceUtility;
import util.trace.misc.ThreadDelayed;
import util.trace.port.PortTraceUtility;
import util.trace.port.consensus.ConsensusTraceUtility;
import util.trace.port.nio.NIOTraceUtility;
import util.trace.port.rpc.gipc.GIPCRPCTraceUtility;
import util.trace.port.rpc.rmi.RMITraceUtility;

public abstract class Constants extends AnAbstractSimulationParametersBean implements ConnectionListener {
    public static final String RMI_RELAYER = "RELAYER";
    public static final String GIPC_RELAYER = "GIPC_RELAYER";

    protected Constants(){setTracing();} //prevents instantiation.
    protected void setTracing() {
        PortTraceUtility.setTracing();
        RMITraceUtility.setTracing();
        GIPCRPCTraceUtility.setTracing();
        NIOTraceUtility.setTracing();
        FactoryTraceUtility.setTracing();
        ConsensusTraceUtility.setTracing();
        ThreadDelayed.enablePrint();
        this.trace(true);
    }
    private void boilerPlate(){
        //Copied from Comp533All.jar/coupledsims/AStandAloneTwoCoupledHalloweenSimulations::start
        SimulationParametersControllerFactory.getSingleton().addSimulationParameterListener(this);
        SimulationParametersControllerFactory.getSingleton().processCommands();
    }
    @Override
    public void connected(String s, ConnectionType connectionType) {
        System.out.println(s + "connected: " + connectionType);
    }

    @Override
    public void notConnected(String s, String s1, ConnectionType connectionType) {
        System.out.println(s + "not-connected: " + connectionType);

    }

    @Override
    public void disconnected(String s, boolean b, String s1, ConnectionType connectionType) {
        System.out.println(s + "disconnected: " + connectionType);
    }
}
