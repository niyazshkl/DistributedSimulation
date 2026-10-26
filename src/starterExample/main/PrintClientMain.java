package starterExample.main;

import assignments.util.mainArgs.ClientArgsProcessor;
import starterExample.PrintClient;

public class PrintClientMain {
    public PrintClientMain() {
    }

    public static void main(String[] args) {
//        (new PrintClient(ClientArgsProcessor.getNIOServerPort(args))).processInput();//why call the .processInput()?
        PrintClient printClient = new PrintClient(ClientArgsProcessor.getNIOServerPort(args));

    }
}