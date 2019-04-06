package kppk.jpx.sys;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * ConsolePrinter is used to print message to system out.
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
            return "ERROR: " + exception.getMessage() + details;
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

        ScheduledExecutorService executor = null;
        ScheduledFuture<?> future = null;
        if (ConsolePrinter.level.value <= level.value) {
            System.out.print(message.get());
            executor = Executors.newSingleThreadScheduledExecutor();
            future = executor.scheduleWithFixedDelay(() -> System.out.print("."),
                    1, 1, TimeUnit.SECONDS);
        }
        task.run();
        if (future != null) {
            future.cancel(true);
            executor.shutdown();
        }
        if (ConsolePrinter.level.value <= level.value) {
            System.out.println("Done");
        }

    }

    public static void println(Verbosity level, Supplier<String> message) {
        if (ConsolePrinter.level.value <= level.value) {
            System.out.println(message.get());
        }
    }

}
