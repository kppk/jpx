package org.jpx.cmd;

import org.jpx.cli.Command;
import org.jpx.cli.StringFlag;
import org.jpx.model.Manifest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

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
        Manifest manifest = Manifest.readFrom(Paths.get(Manifest.NAME));
        //Graph graph = Graph.from(manifest);

        System.out.println(String.format("Compiling %s v%s (%s)",
                manifest.pack.name, manifest.pack.version, manifest.basedir.toAbsolutePath()));

        new JavaProject(manifest.basedir)
                .jdkRelease(JavaProject.JDK.releaseOf(manifest.pack.javaRelease))
                .compile();

        System.out.println("Finished");
    }

    private static void clean() {
        try {
            Path target = Paths.get(JavaProject.DIR_TARGET);
            Files.walk(target)
                    .sorted(Comparator.reverseOrder())
                    .forEach(p -> p.toFile().delete());
        } catch (IOException e) {
            throw new IllegalStateException("Can't delete target directory", e);
        }
    }

    // TODO: refactor this
    public static class JavaProject {

        private static final String DIR_TARGET = "target";
        private static final String DIR_CLASSES = "classes";
        private static final String DIR_SRC = "src";

        private Path srcDir;
        private Path targetDir;
        private Path classesDir;
        private Path baseDir;
        private JDK jdk;

        public JavaProject(Path baseDir) {
            this.baseDir = Objects.requireNonNull(baseDir);
            srcDir = baseDir.resolve(DIR_SRC);
            targetDir = baseDir.resolve(DIR_TARGET);
            classesDir = targetDir.resolve(DIR_CLASSES);
        }

        public JavaProject jdkRelease(JDK release) {
            this.jdk = release;
            return this;
        }

        private Path getJavaHome() {
            return MACOS_JAVA_HOME_PROVIDER.apply(jdk);
        }

        private List<String> getSourceFiles() {
            try {
                return Files
                        .walk(srcDir)
                        .filter(path -> path.getFileName().toString().endsWith(".java"))
                        .map(Path::toString)
                        .collect(Collectors.toList());
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }

        public JavaProject compile() {
            try {
                Files.createDirectories(classesDir);
                List<String> args = new ArrayList<>();
                String javac = getJavaHome().resolve("bin").resolve("javac").toString();
                args.addAll(Arrays.asList(javac,
                        "-d",
                        classesDir.toString(),
                        "-sourcepath",
                        srcDir.toString()
                ));
                args.addAll(getSourceFiles());
                Process p = new ProcessBuilder()
                        .inheritIO()
                        .command(args)
                        .directory(baseDir.toFile())
                        .redirectErrorStream(true)
                        .start();
                p.waitFor();
                return this;
            } catch (IOException | InterruptedException e) {
                throw new IllegalStateException(e);
            }
        }

        public enum JDK {
            REL_8("8"), REL_9("9"), REL_10("10"), REL_11("11");

            public final String release;

            JDK(String name) {
                this.release = name;
            }

            public static JDK releaseOf(String release) {
                Objects.requireNonNull(release);
                return Arrays.asList(values()).stream()
                        .filter(r -> r.release.equals(release))
                        .findFirst()
                        .orElseThrow(() -> new IllegalArgumentException("Unknown java release: " + release));
            }
        }

        interface JavaHomeProvider extends Function<JDK, Path> {
        }

        private static final JavaHomeProvider MACOS_JAVA_HOME_PROVIDER = (jdk -> {
            String versionString = (jdk == JDK.REL_8) ? "1.8" : jdk.release;
            try {
                Process process = new ProcessBuilder()
                        .command("/usr/libexec/java_home",
                                "-v",
                                versionString)
                        .redirectErrorStream(false)
                        .start();
                return readAll(process).stream()
                        .findFirst()
                        .map(Paths::get)
                        .orElseThrow(() -> new IllegalArgumentException("Can't find java home for JDK " + jdk.release));
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        });

        private static List<String> readAll(Process process) {
            List<String> out = new ArrayList<>();

            try (BufferedReader processOutputReader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                String readLine;

                while ((readLine = processOutputReader.readLine()) != null) {
                    out.add(readLine);
                }
                process.waitFor();

            } catch (IOException | InterruptedException e) {
                throw new IllegalStateException(e);
            }

            return out;
        }

    }
}