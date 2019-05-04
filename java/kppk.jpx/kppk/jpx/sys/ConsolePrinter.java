package kppk.jpx.sys;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.function.Supplier;

/**
 * ConsolePrinter is used to print message to system out.
 */
public final class ConsolePrinter {

    public enum Color {
        GREEN("\033[0;32m"),
        YELLOW("\033[0;33m"),
        RED("\033[0;31m");

        private final String code;

        Color(String code) {
            this.code = code;
        }

        public String msg(String message) {
            if (System.console() == null) {
                return message;
            }
            return String.format("%s%s%s", code, message, "\033[0;0m");
        }

    }

    public static Verbosity level = Verbosity.INFO;

    public enum Verbosity {

        ERROR((byte) 10), INFO((byte) 9), VERBOSE((byte) 8);

        private final byte value;

        Verbosity(byte value) {
            this.value = value;
        }
    }

    // note: this might need to be atomic reference at some point
    private static Spinner spinner = null;

    public static void error(Supplier<String> message) {
        println(Verbosity.ERROR, message);
    }

    public static void error(Exception exception) {
        println(Verbosity.ERROR, () -> {
            String details = "";
            if (level == Verbosity.VERBOSE) {
                StringWriter stringWriter = new StringWriter();
                PrintWriter printWriter = new PrintWriter(stringWriter);
                exception.printStackTrace(printWriter);
                details = "\n" + stringWriter.toString();
            }
            return Color.RED.msg("ERROR: ") + exception.getMessage() + details;
        });
    }

    public static void info(Supplier<String> message) {
        println(Verbosity.INFO, message);
    }

    public static void infoWithProgress(Supplier<String> message, Runnable task) {
        printlnWithProgress(Verbosity.INFO, message, task);
    }


    public static void verbose(Supplier<String> message) {
        println(Verbosity.VERBOSE, message);
    }

    public static void printlnWithProgress(Verbosity level, Supplier<String> message, Runnable task) {

        if (ConsolePrinter.level.value <= level.value) {
            spinner = Spinner.newInstance(message.get());
        }
        try {
            // execute the task
            task.run();
        } catch (RuntimeException e) {
            if (ConsolePrinter.level.value <= level.value) {
                spinner.error();
                spinner = null;
            }
            throw e;
        }
        if (ConsolePrinter.level.value <= level.value && spinner != null) {
            spinner.done();
            spinner = null;
        }

    }

    public static void println(Verbosity level, Supplier<String> message) {
        if (ConsolePrinter.level.value <= level.value) {
            if (spinner != null) {
                spinner.addMessage(message.get());
                return;
            }
            System.out.println(message.get());
        }
    }


}
