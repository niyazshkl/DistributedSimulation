package registry;

import assignments.util.mainArgs.RegistryArgsProcessor;
import shared.Constants;
import util.annotations.Tags;
import util.tags.DistributedTags;
import util.trace.port.rpc.rmi.RMIRegistryCreated;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.Scanner;

@Tags({DistributedTags.REGISTRY, DistributedTags.RMI})
public class RMIRegsitry extends Constants implements Registry {
    private int port;
    public RMIRegsitry(){}
    @Override
    public void createRegistry(String[] args){
        try {
            port = RegistryArgsProcessor.getRegistryPort(args);
            RMIRegistryCreated.newCase(this, port);

            LocateRegistry.createRegistry(RegistryArgsProcessor.getRegistryPort(args));

            //Avoids the main thread from termination
            Scanner scanner = new Scanner(System.in);
            scanner.nextLine();
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }
}
