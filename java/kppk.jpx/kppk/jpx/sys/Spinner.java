package kppk.jpx.sys;

import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Spinner can be used to print status of long running tasks.
 * Currently, showing of progress info is not supported.
 * <p>
 * When you create instance, the spinner starts showing it is active.
 */
public interface Spinner {

    /**
     * AddMessage will print additional message while the spinner is running.
     *
     * @param msg Message to print
     */
    void addMessage(String msg);

    /**
     * Stops the spinner.
     */
    void done();

    /**
     * Stops the spinner and signals the task was not successful.
     */
    void error();


    /**
     * Creates new spinner instance and sets the long running task information.
     *
     * @param info Task information
     */
    static Spinner newInstance(String info) {
        if (System.console() == null) {
            return new NoSpinner(info);
        }
        return new TTYSpinner(info);
    }

    /**
     * Spinner which shows the app is active, working on a task.
     * <p>
     * This is done by printing to system out with `\r`.
     */
    final class TTYSpinner implements Spinner {

        private static String[] spinner = new String[]{
                "|",
                "/",
                "-",
                "\\",
                "|",
                "/",
                "-",
                "\\"
        };

        private final String[] messages = new String[spinner.length];
        private final String done;
        private final String error;
        private byte index;

        private final ScheduledExecutorService executor;
        private ScheduledFuture<?> future;
        private final Deque<String> messageQueue = new ConcurrentLinkedDeque<>();

        private TTYSpinner(String message) {
            for (int i = 0; i < spinner.length; i++) {
                messages[i] = String.format("\r%s %s", spinner[i], message);
            }
            done = String.format("\r%s %s", ConsolePrinter.Color.GREEN.msg("✔"), message);
            error = String.format("\r%s %s", ConsolePrinter.Color.RED.msg("✘"), message);
            executor = Executors.newSingleThreadScheduledExecutor();
            future = executor.scheduleWithFixedDelay(this::tick,
                    0, 100, TimeUnit.MILLISECONDS);
        }

        private void tick() {
            while (!messageQueue.isEmpty()) {
                System.out.print(String.format("\r%s\n", messageQueue.removeFirst()));
            }
            System.out.print(messages[index]);
            index++;
            if (index >= spinner.length) {
                index = 0;
            }
        }

        /**
         * This should be used if the spinner is running and we need to print additional message to the console.
         */
        public void addMessage(String msg) {
            messageQueue.addLast(msg);
        }

        public void done() {
            if (!future.isCancelled()) {
                future.cancel(true);
                executor.shutdown();
            }
            System.out.println(done);
        }

        public void error() {
            if (!future.isCancelled()) {
                future.cancel(true);
                executor.shutdown();
            }
            System.out.println(error);
        }


    }

    /**
     * NoSpinner doesn't provide any progress information.
     * This is usable when the app doesn't have tty (e.g. output forwarded to file).
     */
    final class NoSpinner implements Spinner {

        public NoSpinner(String info) {
            System.out.println(info);
        }

        @Override
        public void addMessage(String msg) {
            System.out.println(msg);
        }

        @Override
        public void done() {
            System.out.println("DONE");
        }

        @Override
        public void error() {
            // do nothing, more error details should be printed elsewhere
        }
    }
}
