package kppk.jpx.cmd;

import kppk.cli.BooleanFlag;
import kppk.cli.Command;
import kppk.cli.StringFlag;
import kppk.jpx.model.Manifest;
import kppk.jpx.project.JavaProject;
import kppk.jpx.sys.ConsolePrinter;

import java.nio.file.Paths;

import static kppk.jpx.Main.handleCommon;

/**
 * The `build` cli command is implemented here.
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

        JavaProject project = JavaProject.createNew(manifest);
        ConsolePrinter.infoWithProgress(
                () -> String.format("Compiling %s v%s (%s)",
                        manifest.pack.name,
                        manifest.pack.version,
                        Paths.get(manifest.basedir).toAbsolutePath()),
                project::build
        );

        if (install) {
            ConsolePrinter.infoWithProgress(
                    () -> String.format("Installing %s to ~/.jpx", manifest.pack.name),
                    project::install
            );
        }

        if (docker) {
            ConsolePrinter.infoWithProgress(
                    () -> "Building docker image",
                    () -> project.buildDockerImage(minimize)
            );
        }
    }

    private static void clean() {
        Manifest manifest = Manifest.readFrom(Paths.get("."));
        JavaProject.createNew(manifest).clean();
    }


}