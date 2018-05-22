package org.jpx.cmd;

import org.jpx.cli.Command;
import org.jpx.cli.StringFlag;
import org.jpx.model.Manifest;
import org.jpx.project.JavaProject;

import java.nio.file.Paths;

/**
 * TODO: Document this
 */
public final class Build {

    public static final Command CMD_BUILD = Command.builder()
            .setName("build")
            .setUsage("Compile the current project")
            .setArg(StringFlag.builder()
                    .build())
            .setExecutor(ctx -> build())
            .build();


    public static final Command CMD_CLEAN = Command.builder()
            .setName("clean")
            .setUsage("Remove the target directory")
            .setArg(StringFlag.builder()
                    .build())
            .setExecutor(ctx -> clean())
            .build();

    private static void build() {
        Manifest manifest = Manifest.readFrom(Paths.get("."));
        //Graph graph = Graph.from(manifest);

        System.out.println(String.format("Compiling %s v%s (%s)",
                manifest.pack.name, manifest.pack.version, manifest.basedir.toAbsolutePath()));

        new JavaProject(manifest)
                .compile()
                .doc()
                .pack();

        System.out.println("Finished");
    }

    private static void clean() {
        Manifest manifest = Manifest.readFrom(Paths.get("."));
        new JavaProject(manifest).clean();
    }

}