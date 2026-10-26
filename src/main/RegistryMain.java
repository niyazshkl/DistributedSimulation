package main;

import assignments.util.mainArgs.RegistryArgsProcessor;
import registry.DefaultRegistry;

public class RegistryMain {
    public static void main(String[] args){
        info(args);
        new DefaultRegistry().createRegistry(args);
    }
    public static void info(String[] args){
        System.out.println("Registry Port: " + RegistryArgsProcessor.getRegistryPort(args));
    }
}
