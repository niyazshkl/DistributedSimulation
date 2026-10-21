package server;

import util.trace.port.consensus.RemoteProposeRequestSent;

import java.util.Arrays;

public class ServerArgsProcessor {
    public static final int NIO_PORT_ARG_INDEX = 0;
    public static final int REGISTRY_HOST_ARG_INDEX = 1;
    public static final int REGISTRY_PORT_ARG_INDEX = 2;
    public static final int GIPC_PORT_ARG_INDEX = 3;
    public static final String SERVER_PROXY_NAME = "RELAYER";

    public ServerArgsProcessor() {
    }

    public static String[] removeEmpty(String[] args) {
        return (String[])Arrays.stream(args).filter((s) -> !s.isEmpty()).toArray((var0) -> new String[var0]);
    }

    public static int getNIOServerPort(String[] args) {
        return args.length > 0 ? Integer.parseInt(args[0]) : 9000;
    }

    public static int getServerPort(String[] args) {
        return getNIOServerPort(args);
    }

    public static String getRegistryHost(String[] args) {
        return args.length > 1 ? args[1] : "localhost";
    }

    public static int getRegistryPort(String[] args) {
        return args.length > 2 ? Integer.parseInt(args[2]) : 1099;
    }

    public static int getGIPCServerPort(String[] args) {
        return args.length > 3 ? Integer.parseInt(args[3]) : 10000;
    }
}