package kppk.jpx.cmd;

import kppk.jpx.cli.BooleanFlag;
import kppk.jpx.cli.Command;
import kppk.jpx.cli.StringFlag;
import kppk.jpx.model.Manifest;
import kppk.jpx.project.JavaProject;
import kppk.jpx.sys.ConsolePrinter;

import java.nio.file.Paths;

import static kppk.jpx.Main.handleCommon;

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
            .setExecutor(handleCommon.andThen(ctx -> clean()))
            .build();

    private static void build(boolean install) {
        Manifest manifest = Manifest.readFrom(Paths.get("."));
        //Graph graph = Graph.from(manifest);

        ConsolePrinter.info(() -> String.format("Compiling %s v%s (%s)",
                manifest.pack.name,
                manifest.pack.version,
                Paths.get(manifest.basedir).toAbsolutePath())
        );

        JavaProject project = JavaProject.createNew(manifest)
                .build(true);

        if (install) {
            project.install();
        }

        ConsolePrinter.info(() -> "Finished");
    }

    private static void clean() {
        Manifest manifest = Manifest.readFrom(Paths.get("."));
        JavaProject.createNew(manifest).clean();
    }


}