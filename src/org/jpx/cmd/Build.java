package org.jpx.cmd;

import org.jpx.cli.BooleanFlag;
import org.jpx.cli.Command;
import org.jpx.cli.StringFlag;
import org.jpx.model.Manifest;
import org.jpx.project.JavaProject;
import org.jpx.sys.ConsolePrinter;

import java.nio.file.Paths;

import static org.jpx.Main.handleCommon;

/**
 * TODO: Document this
 */
public final class Build {

    private static BooleanFlag FLAG_INSTALL = BooleanFlag.builder()
            .setName("install")
            .setUsage("Installs built binary.")
            .build();

    public static final Command CMD_BUILD = Command.builder()
            .setName("build")
            .setUsage("Compile the current project")
            .addFlag(FLAG_INSTALL)
            .setArg(StringFlag.builder()
                    .build())
            .setExecutor(handleCommon.andThen(ctx -> build(ctx.getFlagValue(FLAG_INSTALL))))
            .build();


    public static final Command CMD_CLEAN = Command.builder()
            .setName("clean")
            .setUsage("Remove the target directory")
            .setArg(StringFlag.builder()
                    .build())
            .setExecutor(ctx -> clean())
            .build();

    private static void build(boolean install) {
        Manifest manifest = Manifest.readFrom(Paths.get("."));
        //Graph graph = Graph.from(manifest);

        ConsolePrinter.info(() -> String.format("Compiling %s v%s (%s)",
                manifest.pack.name,
                manifest.pack.version,
                Paths.get(manifest.basedir).toAbsolutePath())
        );

        JavaProject.createNew(manifest)
                .build(true);

        ConsolePrinter.info(() -> "Finished");
    }

    private static void clean() {
        Manifest manifest = Manifest.readFrom(Paths.get("."));
        JavaProject.createNew(manifest).clean();
    }


}