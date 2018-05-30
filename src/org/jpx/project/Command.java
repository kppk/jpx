package org.jpx.project;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * TODO: Document this
 */
final class Command {
    public final String command;
    public final List<String> parameters;

    private Command(String command, List<String> parameters) {
        this.command = command;
        this.parameters = parameters;
    }

    private String commandWithPath(Path dir) {
        if (dir == null) {
            return command;
        }
        return dir.resolve(command).toString();
    }

    private Stream<String> args(Path dir) {
        return Stream.concat(
                Stream.of(commandWithPath(dir)),
                parameters.stream()
        );
    }

    public String toStringWithFullPath(Path dir) {
        return args(dir).collect(Collectors.joining(" "));
    }

    public List<String> toListWithFullPath(Path dir) {
        return args(dir).collect(Collectors.toList());
    }

    public static Builder builder(String command) {
        return new Builder(command);
    }

    public static final class Builder {
        private final String command;
        private List<String> params = new ArrayList<>();

        public Builder(String command) {
            this.command = command;
        }

        public Builder addParameter(String param) {
            params.add(param);
            return this;
        }

        public Command build() {
            if (command == null || command.trim().equals("")) {
                throw new IllegalArgumentException("Missing command");
            }
            return new Command(command, Collections.unmodifiableList(params));
        }
    }
}
