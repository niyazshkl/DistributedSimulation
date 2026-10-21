package registry;

import assignments.util.mainArgs.ClientArgsProcessor;

import java.util.Arrays;

public class RegistryArgsProcessor {
    public static final int REGISTRY_PORT_ARG_INDEX = 0;

    public RegistryArgsProcessor() {
    }

    public static String[] removeEmpty(String[] args) {
        return (String[]) Arrays.stream(args).filter((s) -> !s.isEmpty()).toArray((var0) -> new String[var0]);
    }

    public static int getRegistryPort(String[] args) {
        return args.length > 0 ? Integer.parseInt(args[0]) : 1099;
    }
}
