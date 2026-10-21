import coupledsims.ASimulationCoupler;
import coupledsims.TwoCoupledHalloweenSimulations;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
/*
Main relavent classes for A4 are:
Comp533All.jar!/coupledsims/TwoCoupledHalloweenSimulations.class -> Comp533All.jar!/coupledsims/AStandAloneTwoCoupledHalloweenSimulations.class
Comp533All.jar!/stringProcessors/AHalloweenCommandProcessor.class (Observable Subject) <-> Comp533All.jar!/coupledsims/ASimulationCoupler.class (Observer)
 */
public class Main {
    public static void main(String[] args) {
        TwoCoupledHalloweenSimulations.main(args); //Starter code to run with images under src.
    }
}