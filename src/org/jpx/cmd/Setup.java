package org.jpx.cmd;

import org.jpx.cli.Command;
import org.jpx.cli.StringFlag;
import org.jpx.model.Manifest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Following cli commands are implemented here:
 * <ul>
 * <li>new</li>
 * <li>init</li>
 * </ul>
 */
public final class Setup {

    private static final StringFlag FLAG_NAME = StringFlag.builder()
            .setName("name")
            .setUsage("Set the resulting project name, defaults to the value of <path>")
            .build();


    public static final Command CMD_NEW = Command.builder()
            .setName("new")
            .setUsage("Create new project in <path>")
            .setArg(StringFlag.builder()
                    .setName("PATH")
                    .build())
            .addFlag(FLAG_NAME)
            .setExecutor(ctx -> newProject(ctx.getArg(), ctx.getFlagValue(FLAG_NAME)))
            .build();

    public static final Command CMD_INIT = Command.builder()
            .setName("init")
            .setUsage("Create new project in current directory")
            .setExecutor(ctx -> initProject(Paths.get("."), null))
            .build();

    private static void newProject(String dir, String name) {
        try {
            Path dirPath = Paths.get(dir);
            if (Files.exists(dirPath)) {
                throw new IllegalStateException("Directory [" + dir + "] already exists");
            }
            Files.createDirectories(dirPath);
            initProject(dirPath, name);
        } catch (IOException e) {
            throw new IllegalStateException("Can't create directory " + dir);
        }
    }

    private static void initProject(Path dir, String name) {
        writeToFile(dir.resolve(Manifest.NAME), "" +
                "[pack]\n" +
                "name = \"hello_world\" # the name of the package\n" +
                "version = \"0.1.0\"    # the current version, obeying semver\n" +
                "authors = [\"you@example.com\"]\n" +
                "\n" +
                "[bin]\n" +
                "main=\"org.hello.Main\"\n" +
                "\n" +
                "[deps]\n" +
                "dep1=\"1.0.0\"");
        System.out.println("New project initialized in " + dir);
    }

    private static String manifestHead(String name) {
        return String.join("\n",
                "[pack]",
                "name = \"" + name + "\"",
                "version = \"0.1.0\"",
                "authors = [\"you@example.com\"]",
                ""
        );
    }

    private static void writeToFile(Path file, String content) {
        try {
            Files.write(file, content.getBytes());
        } catch (IOException e) {
            throw new IllegalStateException("Can't write to file [" + file + "]", e);
        }
    }
}
