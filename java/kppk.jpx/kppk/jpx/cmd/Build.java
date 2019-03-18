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

    private static BooleanFlag FLAG_DOCKER = BooleanFlag.builder()
            .setName("docker")
            .setUsage("Builds docker container for a binary project.")
            .build();

    private static BooleanFlag FLAG_MINIMIZE = BooleanFlag.builder()
            .setName("minimize")
            .setUsage("Make the target binary minimal (no-debug/minimal docker image).")
            .build();

    public static final Command CMD_BUILD = Command.builder()
            .setName("build")
            .setUsage("Compile the current project")
            .addFlag(FLAG_DOCKER)
            .addFlag(FLAG_INSTALL)
            .addFlag(FLAG_MINIMIZE)
            .setArg(StringFlag.builder()
                    .build())
            .setExecutor(handleCommon.andThen(ctx -> build(
                    ctx.getFlagValue(FLAG_INSTALL),
                    ctx.getFlagValue(FLAG_DOCKER),
                    ctx.getFlagValue(FLAG_MINIMIZE)
            )))
            .build();


    public static final Command CMD_CLEAN = Command.builder()
            .setName("clean")
            .setUsage("Remove the target directory")
            .setArg(StringFlag.builder()
                    .build())
            .setExecutor(handleCommon.andThen(ctx -> clean()))
            .build();

    private static void build(boolean install, boolean docker, boolean minimize) {
        Manifest manifest = Manifest.readFrom(Paths.get("."));
        //Graph graph = Graph.from(manifest);

        ConsolePrinter.info(() -> String.format("Compiling %s v%s (%s)",
                manifest.pack.name,
                manifest.pack.version,
                Paths.get(manifest.basedir).toAbsolutePath())
        );

        JavaProject project = JavaProject.createNew(manifest)
                .build(true, minimize);

        if (install) {
            project.install();
        }

        if (docker) {
            project.buildDockerImage(minimize);
        }

        ConsolePrinter.info(() -> "Finished");
    }

    private static void clean() {
        Manifest manifest = Manifest.readFrom(Paths.get("."));
        JavaProject.createNew(manifest).clean();
    }


}