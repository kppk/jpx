package org.jpx.cmd;

import org.jpx.cli.BooleanFlag;
import org.jpx.cli.Command;
import org.jpx.cli.StringFlag;
import org.jpx.model.Manifest;
import org.jpx.model.Pack;
import org.jpx.project.JavaProject;

import java.io.File;
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

    private static final BooleanFlag FLAG_BINARY = BooleanFlag.builder()
            .setName("bin")
            .setUsage("Use a binary (application) template")
            .build();

    private static final BooleanFlag FLAG_LIBRARY = BooleanFlag.builder()
            .setName("lib")
            .setUsage("Use a library template [default]")
            .build();


    public static final Command CMD_NEW = Command.builder()
            .setName("new")
            .setUsage("Create new project in <path>")
            .setArg(StringFlag.builder()
                    .setName("PATH")
                    .build())
            .addFlag(FLAG_NAME)
            .addFlag(FLAG_BINARY)
            .addFlag(FLAG_LIBRARY)
            .setExecutor(ctx -> newProject(ctx.getArg(),
                    ctx.getFlagValue(FLAG_NAME),
                    asType(ctx.getFlagValue(FLAG_BINARY), ctx.getFlagValue(FLAG_LIBRARY))
            ))
            .build();

    public static final Command CMD_INIT = Command.builder()
            .setName("init")
            .setUsage("Create new project in current directory")
            .addFlag(FLAG_BINARY)
            .addFlag(FLAG_LIBRARY)
            .setExecutor(ctx -> initProject(Paths.get("."),
                    null,
                    asType(ctx.getFlagValue(FLAG_BINARY), ctx.getFlagValue(FLAG_LIBRARY))
            ))
            .build();

    private static void newProject(String dir, String name, Pack.Type type) {
        try {

            Path dirPath = Paths.get(dir);
            if (Files.exists(dirPath)) {
                throw new IllegalStateException("Directory [" + dir + "] already exists");
            }
            if (name == null) {
                name = dir;
            }
            Files.createDirectories(dirPath);
            writeToFile(dirPath.resolve(Manifest.NAME), manifest(name, type));
            name = JavaProject.asModuleName(name);
            Path pkgDir = dirPath.resolve(JavaProject.DIR_SRC).resolve(name).resolve(name.replaceAll("\\.", File.separator));

            Files.createDirectories(pkgDir);
            writeToFile(dirPath.resolve(".gitignore"), String.join("\n",
                    "bin",
                    "target",
                    "lib",
                    ""));
            writeToFile(dirPath.resolve(JavaProject.DIR_SRC).resolve(name).resolve("module-info.java"), String.join("\n",
                    "module " + name + " {",
                    "    requires kppk.somelibrary;",
                    "}",
                    ""));
            if (type == Pack.Type.LIBRARY) {
                writeToFile(pkgDir.resolve("HelloLibrary.java"), String.join("\n",
                        "package " + name + ";",
                        "",
                        "public class HelloLibrary {",
                        "",
                        "   public void printHello() {",
                        "       System.out.println(\"hello world!\");",
                        "   }",
                        "",
                        "}"
                ));
            }
            if (type == Pack.Type.BINARY) {
                writeToFile(pkgDir.resolve("Main.java"), String.join("\n",
                        "package " + name + ";",
                        "",
                        "public class Main {",
                        "",
                        "   public static void main(String[] args) {",
                        "       System.out.println(\"hello world!\");",
                        "   }",
                        "",
                        "}"
                ));
            }
        } catch (IOException e) {
            throw new IllegalStateException("Can't create directory " + dir);
        }
    }

    private static Pack.Type asType(Boolean binary, Boolean library) {
        if (binary && library) {
            throw new IllegalArgumentException("Must be either binary or library");
        }
        return binary ? Pack.Type.BINARY : Pack.Type.LIBRARY;
    }

    private static void initProject(Path dir, String name, Pack.Type type) {
        if (name == null) {
            name = dir.toString();
        }
        name = JavaProject.asModuleName(name);
        writeToFile(dir.resolve(Manifest.NAME), manifest(name, type));
        System.out.println("New " + type.name() + " project initialized in " + dir);
    }

    private static String manifest(String name, Pack.Type type) {
        return String.join("\n",
                "[pack]",
                "name = '" + JavaProject.asModuleName(name) + "'",
                "version = '1.0.0'",
                "type = '" + type.name + "'",
                "java_release = '9'",
                "",
                "[deps]",
                "'kppk.somelibrary' = '1.0.0 <= v < 2.0.0'"
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
