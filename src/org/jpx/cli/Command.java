package org.jpx.cli;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

/**
 * TODO: Document this
 */
public final class Command {

    private String name;
    private String shortName;
    private String usage;
    private List<Flag> flags;
    private Flag arg;
    private Consumer<Context> executor;

    public String getName() {
        return name;
    }

    public String getShortName() {
        return shortName;
    }

    public String getUsage() {
        return usage;
    }

    public List<Flag> getFlags() {
        return flags;
    }

    public Flag getArg() {
        return arg;
    }

    public void execute(Context context) {
        executor.accept(context);
    }

    private Command(String name, String shortName, String usage, Flag arg, List<Flag> flags, Consumer<Context> executor) {
        this.name = name;
        this.shortName = shortName;
        this.usage = usage;
        this.flags = flags;
        this.arg = arg;
        this.executor = executor;
    }

    boolean matches(String name) {
        return name.equals(getName()) ||
                name.equals(getShortName());
    }

    public static CommandBuilder builder() {
        return new CommandBuilder();
    }

    public static class CommandBuilder {
        private String name;
        private String shortName;
        private String usage;
        private List<Flag> flags = new LinkedList<>();
        private Consumer<Context> executor;
        private Flag arg;

        public CommandBuilder setName(String name) {
            this.name = name;
            return this;
        }

        public CommandBuilder setShortName(String shortName) {
            this.shortName = shortName;
            return this;
        }

        public CommandBuilder setUsage(String usage) {
            this.usage = usage;
            return this;
        }

        public CommandBuilder setArg(Flag arg) {
            this.arg = arg;
            return this;
        }

        public CommandBuilder addFlag(Flag flag) {
            this.flags.add(flag);
            return this;
        }

        public CommandBuilder setExecutor(Consumer<Context> executor) {
            this.executor = executor;
            return this;
        }

        public Command build() {
            return new Command(name, shortName, usage, arg, flags, executor);
        }
    }
}
