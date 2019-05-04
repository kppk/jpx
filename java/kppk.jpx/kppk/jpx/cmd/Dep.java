package kppk.jpx.cmd;

import kppk.cli.Command;
import kppk.jpx.dep.Dependency;
import kppk.jpx.dep.Graph;
import kppk.jpx.model.Manifest;
import kppk.jpx.project.JavaProject;
import kppk.jpx.sys.ConsolePrinter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static kppk.jpx.Main.handleCommon;

/**
 * `install` cli command - install dependencies to the current project directory.
 */
public final class Dep {

    public static final Command CMD_DEP_INSTALL = Command.builder()
            .setName("install")
            .setUsage("Install all dependencies to libs directory")
            .setExecutor(handleCommon.andThen(ctx -> installDeps()))
            .build();

    private Dep() {
    }

    private static void installDeps() {

        Path current = Paths.get(".");
        Manifest mf = Manifest.readFrom(current);

        ConsolePrinter.infoWithProgress(
                () -> String.format("Installing dependencies for %s (%s)",
                        mf.pack.name, Paths.get(mf.basedir).toAbsolutePath()),
                () -> {
                    Graph graph = Graph.from(JavaProject.createNew(mf));
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
                            .forEach(dependency -> dependency.resolver.fetch(dependency.version, lib));

                });

    }
}
