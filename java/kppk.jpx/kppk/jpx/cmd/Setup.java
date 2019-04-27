package kppk.jpx.cmd;

import kppk.cli.BooleanFlag;
import kppk.cli.Command;
import kppk.cli.StringFlag;
import kppk.jpx.model.Manifest;
import kppk.jpx.model.Pack;
import kppk.jpx.project.JavaProject;
import kppk.jpx.sys.ConsolePrinter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static kppk.jpx.Main.handleCommon;

/**
 * Following cli commands are implemented here:
 * <ul>
 * <li>new</li>
 * <li>init</li>
 * </ul>
 */
public final class Setup {

    private static final StringFlag FLAG_NAME = StringFlag.builder()
            .setName("repo")
            .setUsage("Set the resulting project repo, defaults to the value of <path>")
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
            .setExecutor(handleCommon.andThen(ctx -> newProject(ctx.getArg(),
                    ctx.getFlagValue(FLAG_NAME),
                    asType(ctx.getFlagValue(FLAG_BINARY), ctx.getFlagValue(FLAG_LIBRARY))
            )))
            .build();

    public static final Command CMD_INIT = Command.builder()
            .setName("init")
            .setUsage("Create new project in current directory")
            .addFlag(FLAG_NAME)
            .addFlag(FLAG_BINARY)
            .addFlag(FLAG_LIBRARY)
            .setExecutor(handleCommon.andThen(ctx -> initProject(Paths.get("."),
                    ctx.getFlagValue(FLAG_NAME),
                    asType(ctx.getFlagValue(FLAG_BINARY), ctx.getFlagValue(FLAG_LIBRARY))
            )))
            .build();

    private static void newProject(String dir, String projectName, Pack.Type type) {
        try {
            if (dir == null) {
                throw new IllegalArgumentException("Missing argument <path>");
            }
            Path dirPath = Paths.get(dir);
            String n = projectName == null ? dir : projectName;
            Pack.Name name = Pack.Name.parse(n);
            ConsolePrinter.info(() -> String.format("Setting up new *%s* project %s (%s)",
                    type.name,
                    name,
                    dirPath.toAbsolutePath()));

            if (Files.exists(dirPath)) {
                throw new IllegalStateException("Directory [" + dir + "] already exists");
            }

            Files.createDirectories(dirPath);
            writeToFile(dirPath.resolve(Manifest.NAME), manifest(name, type));
            String module = JavaProject.asModuleName(name);
            Path pkgDir = dirPath.resolve(JavaProject.DIR_SRC).resolve(module).resolve(name.org).resolve(name.repo);

            Files.createDirectories(pkgDir);
            writeToFile(dirPath.resolve(".gitignore"), String.join("\n",
                    "bin",
                    "target",
                    "lib",
                    ""));
            writeToFile(dirPath.resolve(JavaProject.DIR_SRC).resolve(module).resolve("module-info.java"), String.join("\n",
                    "module " + module + " {",
                    "    requires kppk.somelibrary;",
                    "}",
                    ""));
            if (type == Pack.Type.LIBRARY) {
                writeToFile(pkgDir.resolve("HelloLibrary.java"), String.join("\n",
                        "package " + module + ";",
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
                        "package " + module + ";",
                        "import kppk.somelibrary.HelloLibrary;",
                        "",
                        "public class Main {",
                        "",
                        "   public static void main(String[] args) {",
                        "       System.out.println(\"hello world!\");",
                        "       HelloLibrary hello = new HelloLibrary();",
                        "           hello.printHello();",
                        "   }",
                        "",
                        "}"
                ));
            }

            ConsolePrinter.info(() -> "Finished");
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

    private static void initProject(Path dir, String projectName, Pack.Type type) {
        String n = projectName == null ? dir.toString() : projectName;
        Pack.Name name = Pack.Name.parse(n);
        ConsolePrinter.info(() -> String.format("Initializing *%s* project %s (%s)",
                type.name,
                name,
                dir.toAbsolutePath()));
        writeToFile(dir.resolve(Manifest.NAME), manifest(name, type));
        ConsolePrinter.info(() -> "Finished");
    }

    private static String manifest(Pack.Name name, Pack.Type type) {
        return String.join("\n",
                "[pack]",
                "name = '" + name.toString() + "'",
                "version = '1.0.0'",
                "type = '" + type.name + "'",
                "java_release = '12'"
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
