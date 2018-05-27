package org.jpx.cmd;

import org.jpx.cli.Command;
import org.jpx.dep.Dependency;
import org.jpx.dep.Graph;
import org.jpx.model.Manifest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

/**
 * TODO: Document this
 */
public final class Dep {

    public static final Command CMD_DEP_INSTALL = Command.builder()
            .setName("install")
            .setUsage("Install all dependencies to libs directory")
            .setExecutor(ctx -> installDeps())
            .build();

    private Dep() {
    }

    private static void installDeps() {
        Path current = Paths.get(".");
        Manifest mf = Manifest.readFrom(current);
        Graph graph = Graph.from(mf);
        List<Dependency> list = graph.flatten();

        Path libs = current.resolve("lib");
        try {
            if (!Files.exists(libs)) {
                Files.createDirectory(libs);
            }
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }

        List<String> fetched = list.stream()
                .filter(d -> d.resolver != null)
                .map(d -> d.resolver.fetch(libs))
                .collect(Collectors.toList());


        // to install dependencies:
        // parse toml
        // resolve all
        // flatten
        // for each dependency do fetch on resolver
        // write lock file
    }
}
