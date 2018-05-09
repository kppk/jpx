package org.jpx.cli;

import java.io.PrintStream;
import java.util.Formatter;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Interface to implement to provide usage printer.s
 */
@FunctionalInterface
public interface UsagePrinter<T> {

    void print(T target, PrintStream out);

    UsagePrinter<App> USAGE_PRINTER_APP = (app, out) -> {
        Formatter formatter = new Formatter(out);
        formatter.format("%s\n", app.getUsage());
        formatter.format("\n");
        formatter.format("Usage:\n");
        formatter.format("\t%s [options]\n", app.getName());
        formatter.format("\n");
        formatter.format("Options:\n");
        app.getFlags().forEach(f -> {
            formatter.format("\t%-20s%s\n", concat(prefix("-", f.getShortName()), prefix("--", f.getName())), f.getUsage());
        });
        formatter.format("\n");
        formatter.format("Commands:\n");
        app.getCommands().forEach(c -> {
            formatter.format("\t%-20s%s\n", concat(c.getShortName(), c.getName()), c.getUsage());
        });
    };

    UsagePrinter<Command> USAGE_PRINTER_CMD = (cmd, out) -> {

    };

    // move/remove
    static String concat(String... str) {
        return Stream.of(str).filter(Objects::nonNull).collect(Collectors.joining(","));
    }

    // move/remove
    static String prefix(String prefix, String s) {
        if (s != null) {
            return prefix + s;
        }
        return null;
    }
}
