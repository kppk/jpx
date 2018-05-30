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
        List<Dependency> dependencies = graph.flatten();

        Path lib = current.resolve("lib");
        try {
            if (!Files.exists(lib)) {
                Files.createDirectory(lib);
            }
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }

        dependencies.stream()
                .filter(d -> d.resolver != null)
                .forEach(dependency -> {
                    dependency.resolver.fetch(dependency.version, lib);
                });


        // to install dependencies:
        // parse toml
        // resolve all
        // flatten
        // for each dependency do fetch on resolver
        // write lock file
    }
}
