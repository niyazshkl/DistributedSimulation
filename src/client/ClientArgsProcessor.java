package client;

import util.trace.port.consensus.ProposalMade;

import java.util.Arrays;

public class ClientArgsProcessor {
    public static final int HOST_ARG_INDEX = 0;
    public static final int NIO_PORT_ARG_INDEX = 1;
    public static final int CLIENT_NAME_ARG_INDEX = 2;
    public static final String DEFAULT_CLIENT_NAME = "Generic Client:" + Math.random();
    public static final String DEFAULT_HEADLESS_VALUE = "false";
    public static final int HEADLESS_ARG_INDEX = 3;
    public static final int REGISTRY_HOST_ARG_INDEX = 4;
    public static final int REGISTRY_PORT_ARG_INDEX = 5;
    public static final int GIPC_PORT_ARG_INDEX = 6;
    public static final int ACCEPT_IPC_CHANGE_ARG_INDEX = 7;
    public static final String DEFAULT_ACCEPT_IPC_CHANGE_VALUE = "true";
    public static final String SERVER_PROXY_NAME = "RELAYER";

    public ClientArgsProcessor() {

    }

    public static String[] removeEmpty(String[] args) {
        return (String[])Arrays.stream(args).filter((s) -> !s.isEmpty()).toArray((var0) -> new String[var0]);
    }

    public static String getServerHost(String[] args) {
        return args.length > 0 ? args[0] : "localhost";
    }

    public static int getNIOServerPort(String[] args) {
        return args.length > 1 ? Integer.parseInt(args[1]) : 9000;
    }

    public static int getServerPort(String[] args) {
        return getNIOServerPort(args);
    }

    public static String getClientName(String[] args) {
        return args.length > 2 ? args[2] : DEFAULT_CLIENT_NAME;
    }

    public static String getHeadless(String[] args) {
        return args.length > 3 ? args[3] : "false";
    }

    public static String getRegistryHost(String[] args) {
        return args.length > 4 ? args[4] : getServerHost(args);
    }

    public static int getRegistryPort(String[] args) {
        return args.length > 5 ? Integer.parseInt(args[5]) : 1099;
    }

    public static int getGIPCPort(String[] args) {
        return args.length > 6 ? Integer.parseInt(args[6]) : 10000;
    }
}

