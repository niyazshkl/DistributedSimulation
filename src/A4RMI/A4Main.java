package A4RMI;

import java.io.IOException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.RemoteException;

public class A4Main {

    public static void main(String[] args) throws Exception {
        // 1. Start registry and wait until it responds.
        startTerminal("server.RMIRegsitry", args);
        waitForRegistry("localhost", 1099);

        // 2. Start server and wait until its remote object is registered.
        startTerminal("server.RMIServer", args);
        waitForServer("localhost", 1099, "RELAYER");

        // 3. Start both clients independently.
        startTerminal("client.RMIClient", new String[]{"Client1"});
        startTerminal("client.RMIClient", new String[]{"Client2"});
    }

    private static void startTerminal(String className, String[] args)
            throws IOException {

        String java = System.getProperty("java.home") + "/bin/java";
        String classpath = System.getProperty("java.class.path");

        StringBuilder command = new StringBuilder();
        command.append("\"").append(java).append("\" -cp \"")
                .append(classpath).append("\" ")
                .append(className);

        for (String arg : args) {
            command.append(" \"").append(arg).append("\"");
        }

        String script = "tell application \"Terminal\"\n"
                + "activate\n"
                + "do script " + quoteAppleScript(command.toString()) + "\n"
                + "end tell";

        new ProcessBuilder("osascript", "-e", script).start();
    }

    private static void waitForRegistry(String host, int port)
            throws InterruptedException {

        while (true) {
            try {
                LocateRegistry.getRegistry(host, port).list();
                System.out.println("Registry is ready.");
                return;
            } catch (RemoteException e) {
                Thread.sleep(500);
            }
        }
    }

    private static void waitForServer(String host, int port, String name)
            throws InterruptedException, RemoteException {

        Registry registry = LocateRegistry.getRegistry(host, port);

        while (true) {
            try {
                registry.lookup(name);
                System.out.println("Server is ready.");
                return;
            } catch (Exception e) {
                Thread.sleep(500);
            }
        }
    }

    private static String quoteAppleScript(String value) {
        return "\"" + value.replace("\\", "\\\\")
                .replace("\"", "\\\"") + "\"";
    }
}