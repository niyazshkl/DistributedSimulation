package main;

import assignments.util.mainArgs.ClientArgsProcessor;
import client.DefaultClient;
import util.annotations.Tags;
import util.tags.DistributedTags;

@Tags({DistributedTags.CLIENT, DistributedTags.RMI, DistributedTags.GIPC, DistributedTags.NIO})
public class ClientMain {
    public static void main(String[] args){
        info(args);
        DefaultClient.initializeClient(args);
    }

    public static void info(String[] args) {
        System.out.println("Registry Host: " + ClientArgsProcessor.getRegistryHost(args));
        System.out.println("Registry Port: " + ClientArgsProcessor.getRegistryPort(args));
        System.out.println("Server Host: " + ClientArgsProcessor.getServerHost(args));
        System.out.println("Client Name: " + ClientArgsProcessor.getClientName(args));
        System.out.println("Headless: " + ClientArgsProcessor.getHeadless(args));
        System.out.println("GIPC Port: " + ClientArgsProcessor.getGIPCPort(args));
        System.out.println("NIO Server Port: " + ClientArgsProcessor.getNIOServerPort(args));
    }
}
