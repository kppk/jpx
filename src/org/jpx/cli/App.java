package org.jpx.cli;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Formatter;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * TODO: Document this
 */
public final class App {


    private static final StringFlag FLAG_HELP = StringFlag.builder()
            .setName("help")
            .setShortName("h")
            .setUsage("Display this message")
            .build();

    private final String name;
    private final String usage;
    private final List<Flag> flags;
    private final List<Command> commands;

    private App(String name, String usage, List<Flag> flags, List<Command> commands) {
        this.name = name;
        this.usage = usage;
        this.flags = flags;
        this.commands = commands;
    }

    public String getName() {
        return name;
    }

    public String getUsage() {
        return usage;
    }

    public List<Flag> getFlags() {
        return flags;
    }

    public List<Command> getCommands() {
        return commands;
    }

    public void execute(String args[]) {
        Arguments arguments = new Arguments(args);
        if (arguments.isEmpty()) {
            printUsage(System.out);
            return;
        }
        Context.ContextBuilder contextBuilder = Context.builder();
        contextBuilder.setApp(this);
        while (arguments.hasNext()) {
            String arg = arguments.next();
            if (!parseFlag(arg, arguments, contextBuilder, flags) &&
                    !parseCommand(arg, arguments, contextBuilder, flags)) {
                throw new IllegalArgumentException("Illegal argument " + arg);
            }
        }

    }

    private boolean parseCommand(String arg,
                                 Arguments arguments,
                                 Context.ContextBuilder contextBuilder,
                                 List<Flag> flags) {
        Optional<Command> cmdOpt = commands.stream().filter(c -> c.matches(arg)).findFirst();
        if (cmdOpt.isPresent()) {
            Command cmd = cmdOpt.get();
            contextBuilder.setCommand(cmd);
            List<Flag> allFlags = concat(flags, cmd.getFlags());
            while (arguments.hasNext()) {
                String cmdArg = arguments.next();
                if (!parseFlag(cmdArg, arguments, contextBuilder, allFlags)) {
                    if (cmd.getArg() != null) {
                        contextBuilder.setArg(cmdArg);
                    } else {
                        throw new IllegalArgumentException("Unexpected argument " + cmdArg);
                    }
                }
            }
            cmd.execute(contextBuilder.build());
            return true;
        }
        return false;
    }

    private boolean parseFlag(String arg,
                              Arguments arguments,
                              Context.ContextBuilder contextBuilder,
                              List<Flag> flags) {
        if (Flag.isFlag(arg)) {
            Optional<Flag> flag = flags.stream().filter(f -> f.matches(arg)).findFirst();
            if (!flag.isPresent()) {
                throw new IllegalArgumentException("Missing value for " + flag.get().getName());
            }
            // check next is available and it is not flag
            if (arguments.hasNext() && !Flag.isFlag(arguments.peek())) {
                contextBuilder.addValue(flag.get(), arguments.next());
                return true;
            } else if (flag.get() instanceof BooleanFlag) {
                // boolean flag can be without the value
                contextBuilder.addValue(flag.get(), "true");
            }
            throw new IllegalArgumentException("Missing value for flag " + flag.get().getName());
        }
        return false;
    }

    private static <T> List<T> concat(List<T>... list) {
        return Stream.of(list).flatMap(Collection::stream).collect(Collectors.toList());
    }

    private void printUsage(PrintStream out) {
        Formatter formatter = new Formatter(out);
        formatter.format("%s\n", usage);
        formatter.format("\n");
        formatter.format("Usage:\n");
        formatter.format("\t%s [options]\n", name);
        formatter.format("\n");
        formatter.format("Options:\n");
        flags.forEach(f -> {
            formatter.format("\t%-20s%s\n", concat(prefix("-", f.getShortName()), prefix("--", f.getName())), f.getUsage());
        });
        formatter.format("\n");
        formatter.format("Commands:\n");
        commands.forEach(c -> {
            formatter.format("\t%-20s%s\n", concat(c.getShortName(), c.getName()), c.getUsage());
        });
    }

    private String concat(String... str) {
        return Stream.of(str).filter(Objects::nonNull).collect(Collectors.joining(","));
    }

    private String prefix(String prefix, String s) {
        if (s != null) {
            return prefix + s;
        }
        return null;
    }


    public static AppBuilder builder() {
        return new AppBuilder();
    }

    public static class AppBuilder {
        private String name;
        private String usage;
        private List<Flag> flags = new ArrayList<>();
        private List<Command> commands = new ArrayList<>();

        private AppBuilder() {
            // always add help flag
            flags.add(FLAG_HELP);
        }

        public AppBuilder setName(String name) {
            this.name = name;
            return this;
        }

        public AppBuilder setUsage(String usage) {
            this.usage = usage;
            return this;
        }

        public AppBuilder addFlag(Flag flags) {
            this.flags.add(flags);
            return this;
        }

        public AppBuilder addCommand(Command command) {
            this.commands.add(command);
            return this;
        }

        public App build() {
            return new App(name, usage, flags, commands);
        }
    }

}
