package main;

import assignments.util.mainArgs.ServerArgsProcessor;
import server.RMIServer;
import util.annotations.Tags;
import util.tags.DistributedTags;

@Tags({DistributedTags.SERVER, DistributedTags.RMI})
public class ServerMain {
    public static void main(String[] args){
        info(args);
        new RMIServer().launchServer(args);
    }

    public static void info(String[] args) {
        System.out.println("Registry Host: " + ServerArgsProcessor.getRegistryHost(args));
        System.out.println("Registry Port: " + ServerArgsProcessor.getRegistryPort(args));
        System.out.println("NIO Server Port: " + ServerArgsProcessor.getNIOServerPort(args));
        System.out.println("GIPC Server Port: " + ServerArgsProcessor.getGIPCServerPort(args));
    }
}
