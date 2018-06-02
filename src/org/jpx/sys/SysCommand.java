package org.jpx.sys;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * TODO: Document this
 */
public final class SysCommand {
    public final String command;
    public final List<String> parameters;
    public final SysCommand pipeTo;

    private SysCommand(String command, List<String> parameters) {
        this(command, parameters, null);
    }

    private SysCommand(String command, List<String> parameters, SysCommand pipeTo) {
        this.command = command;
        this.parameters = parameters;
        this.pipeTo = pipeTo;
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
        String str = args(dir).collect(Collectors.joining(" "));
        if (pipeTo != null) {
            return str + "|" + pipeTo.toStringWithFullPath(dir);
        }
        return str;
    }

    public List<String> toListWithFullPath(Path dir) {
        if (pipeTo != null) {
            return Arrays.asList(
                    "/bin/bash",
                    "-l",
                    "-c",
                    toStringWithFullPath(dir)
            );
        }
        return args(dir).collect(Collectors.toList());
    }

    public static Builder builder(String command) {
        return new Builder(command);
    }

    public static final class Builder {
        private final String command;
        private List<String> params = new ArrayList<>();
        private Builder pipeTo;
        private final Builder root;

        private Builder(String command) {
            this.command = command;
            this.root = this;
        }

        private Builder(Builder root, String command) {
            this.command = command;
            this.root = root;
        }

        public Builder addParameter(String param) {
            params.add(param);
            return this;
        }

        public Builder pipeTo(String command) {
            pipeTo = new Builder(root, command);
            return pipeTo;
        }

        public SysCommand build() {
            if (command == null || command.trim().equals("")) {
                throw new IllegalArgumentException("Missing command");
            }
            return root.doBuild();
        }

        private SysCommand doBuild() {
            return new SysCommand(command, Collections.unmodifiableList(params), pipeTo != null ? pipeTo.doBuild() : null);
        }
    }
}
