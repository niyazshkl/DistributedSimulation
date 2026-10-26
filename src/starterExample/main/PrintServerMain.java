package starterExample.main;

import assignments.util.mainArgs.ServerArgsProcessor;
import starterExample.PrintServer;

public class PrintServerMain {
public PrintServerMain() {
}

public static void main(String[] args) {
    new PrintServer(ServerArgsProcessor.getNIOServerPort(args));
}
}