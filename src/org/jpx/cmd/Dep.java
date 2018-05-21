package org.jpx.cmd;

import org.jpx.cli.Command;
import org.jpx.dep.Dependency;
import org.jpx.dep.Graph;
import org.jpx.model.Manifest;

import java.nio.file.Paths;
import java.util.List;

/**
 * TODO: Document this
 */
public final class Dep {

    public static final Command CMD_DEP_INSTALL = Command.builder()
            .setName("dep-install")
            .setUsage("Install all dependencies to libs directory")
            .setExecutor(ctx -> installDeps())
            .build();

    private Dep() {
    }

    private static void installDeps() {
        Manifest mf = Manifest.readFrom(Paths.get("."));
        Graph graph = Graph.from(mf);
        List<Dependency> list = graph.flatten();


        // to install dependencies:
        // parse toml
        // resolve all
        // flatten
        // for each dependency do fetch on resolver
        // write lock file
    }
}
