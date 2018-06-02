package org.jpx.sys;

import java.util.function.Supplier;

/**
 * TODO: Document this
 */
public final class ConsolePrinter {

    public static Verbosity level = Verbosity.INFO;

    public enum Verbosity {

        ERROR((byte) 10), INFO((byte) 9), VERBOSE((byte) 8);

        private final byte value;

        Verbosity(byte value) {
            this.value = value;
        }
    }

    public static void error(Supplier<String> message) {
        print(Verbosity.ERROR, message);
    }

    public static void error(Exception exception) {
        print(Verbosity.ERROR, () -> "ERROR: " + exception.getMessage());
    }

    public static void info(Supplier<String> message) {
        print(Verbosity.INFO, message);
    }

    public static void verbose(Supplier<String> message) {
        print(Verbosity.VERBOSE, message);
    }


    public static void print(Verbosity level, Supplier<String> message) {
        if (ConsolePrinter.level.value >= level.value) {
            System.out.println(message.get());
        }
    }

}
