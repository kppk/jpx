package org.jpx.cli;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * TODO: Document this
 */
public final class Context {

    private App app;
    private Command command;
    private Map<Flag, String> values;
    private String arg;

    public String getArg() {
        return arg;
    }

    public <T> T getFlagValue(Flag<T> flag) {
        return values.entrySet().stream()
                .filter(entry -> entry.getKey().getName().equals(flag))
                .findFirst()
                .map(entry -> flag.convert(entry.getValue()))
                .orElse(flag.getDefaultValue());
    }

    public Map<String, String> getFlagValues() {
        return values.entrySet().stream()
                .map(entry -> new HashMap.SimpleEntry<>(entry.getKey().getName(), entry.getValue()))
                .collect(Collectors.toMap(entry -> entry.getKey(), entry -> entry.getValue()));
    }


    private Context(App app, Command command, String arg, Map<Flag, String> values) {
        this.app = app;
        this.command = command;
        this.arg = arg;
        this.values = values;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Context{");
        sb.append("app=").append(app);
        sb.append(", command=").append(command);
        sb.append(", values=").append(values);
        sb.append(", arg='").append(arg).append('\'');
        sb.append('}');
        return sb.toString();
    }

    public static ContextBuilder builder() {
        return new ContextBuilder();
    }

    public static class ContextBuilder {

        private App app;
        private Map<Flag, String> values = new HashMap<>();
        private Command command;
        private String arg;

        private ContextBuilder() {
        }

        public ContextBuilder setApp(App app) {
            this.app = app;
            return this;
        }

        public ContextBuilder setCommand(Command command) {
            this.command = command;
            return this;
        }

        public ContextBuilder setArg(String value) {
            this.arg = value;
            return this;
        }

        public ContextBuilder addValue(Flag flag, String value) {
            values.put(flag, value);
            return this;
        }

        public Context build() {
            return new Context(app, command, arg, values);
        }


    }
}
