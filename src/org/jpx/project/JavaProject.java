package org.jpx.project;

import org.jpx.model.Manifest;
import org.jpx.model.Pack;

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
import java.util.stream.Stream;

/**
 * TODO: Document this
 */
public final class JavaProject {

    static final String DIR_TARGET = "target";
    static final String DIR_CLASSES = "classes";
    static final String DIR_MOD = "mod";
    static final String DIR_DOC = "doc";
    public static final String DIR_SRC = "java";
    static final String DIR_LIB = "lib";

    final Path srcDir;
    final Path targetDir;
    final Path classesDir;
    final Path baseDir;
    final Path docDir;
    final Path modTargetDir;
    final Path modSrcDir;
    final Manifest manifest;
    final JDK jdk;
    final Path javaHome;
    final Path libDir;
    final String name;

    private JavaProject(Manifest manifest,
                        String name,
                        Path baseDir,
                        JDK jdk,
                        Path javaHome) {
        this.baseDir = baseDir;
        this.name = name;
        this.manifest = manifest;
        this.srcDir = baseDir.resolve(DIR_SRC).resolve(name);
        this.targetDir = baseDir.resolve(DIR_TARGET);
        this.classesDir = targetDir.resolve(DIR_CLASSES);
        this.docDir = targetDir.resolve(DIR_DOC);
        this.libDir = baseDir.resolve(DIR_LIB);
        this.jdk = jdk;
        this.javaHome = javaHome;
        this.modTargetDir = targetDir.resolve(DIR_MOD);
        this.modSrcDir = baseDir.resolve(DIR_SRC);

    }

    public static JavaProject createNew(Manifest mf) {
        Objects.requireNonNull(mf);

        // todo: validate pack name
        // todo: validate basedir
        String name = mf.pack.name;
        Path baseDir = mf.basedir;
        JavaProject.JDK jdk = JavaProject.JDK.releaseOf(mf.pack.javaRelease);
        Path javaHome = MACOS_JAVA_HOME_PROVIDER.apply(jdk);

        return new JavaProject(mf, name, baseDir, jdk, javaHome);
    }

    public List<String> getSourceFiles() {
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

    public List<String> getModuleDirs() {
        try {
            Stream<Path> projectMods = Stream.of(modSrcDir);

            Stream<Path> libMods = Stream.empty();
            if (Files.exists(libDir)) {
                libMods = Files.list(libDir)
                        .filter(p -> Files.isDirectory(p) && Files.exists(p.resolve(DIR_SRC)))
                        .map(p -> p.resolve(DIR_SRC));
            }

            return Stream.concat(projectMods, libMods)
                    .map(baseDir::relativize)
                    .map(Path::toString)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public JavaProject compile() {
        Compiler.getCompiler(jdk).compile(this);
        return this;
    }

    public JavaProject doc() {
        try {
            String javadoc = javaHome.resolve("bin").resolve("javadoc").toString();
            Files.createDirectories(docDir);
            List<String> args = new ArrayList<>();
            args.addAll(Arrays.asList(javadoc,
                    "-d",
                    docDir.toString(),
                    "-sourcepath",
                    srcDir.toString(),
                    "-linksource"
            ));
            args.addAll(getSourceFiles());
            Process p = new ProcessBuilder()
                    //.inheritIO()
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

    /**
     * Creates a package (jar, native, ...)
     *
     * @return this
     */
    public JavaProject pack() {
        try {
            Path metaInf = targetDir.resolve("META-INF").resolve("jpx");
            removeDir(metaInf);
            Files.createDirectories(metaInf);
            Path metaInfDoc = metaInf.resolve(DIR_DOC);
            Path metaInfSrc = metaInf.resolve(DIR_SRC);
            Files.createDirectory(metaInfDoc);
            Files.createDirectory(metaInfSrc);
            copy(docDir, metaInfDoc);
            copy(srcDir, metaInfSrc);
            copy(manifest.basedir.resolve(Manifest.NAME), metaInf.resolve(Manifest.NAME));
            String jar = javaHome.resolve("bin").resolve("jar").toString();
            Process p = new ProcessBuilder()
                    //.inheritIO()
                    .command(jar,
                            "cfv",
                            getLibrary().toString(),
                            "-C",
                            classesDir.toString(),
                            ".",
                            "-C",
                            targetDir.toString(),
                            targetDir.relativize(metaInf).toString())
                    .directory(baseDir.toFile())
                    .redirectErrorStream(true)
                    .start();
            p.waitFor();
            removeDir(metaInf);
        } catch (IOException | InterruptedException e) {
            throw new IllegalStateException(e);
        }
        return this;
    }

    public JavaProject clean() {
        removeDir(targetDir);
        return this;
    }

    public boolean isLibrary() {
        return manifest.pack.type == Pack.Type.LIBRARY;
    }

    public Path getLibrary() {
        return targetDir.resolve(manifest.pack.name + ".jar");
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

    private static final JavaHomeProvider MACOS_JAVA_HOME_PROVIDER = jdk -> {
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
    };

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

    private static void copy(Path src, Path dest) {
        try {
            if (!Files.isDirectory(src)) {
                Files.copy(src, dest);
                return;
            }
            Files.walk(src)
                    .forEach(s -> {
                        Path d = dest.resolve(src.relativize(s));
                        try {
                            if (Files.isDirectory(s)) {
                                Files.createDirectories(d);
                            } else {
                                Files.copy(s, d);
                            }
                        } catch (IOException e) {
                            throw new IllegalStateException(e);
                        }
                    });
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private static void removeDir(Path dir) {
        if (!Files.exists(dir)) {
            return;
        }
        try {
            Files.walk(dir)
                    .sorted(Comparator.reverseOrder())
                    .forEach(p -> p.toFile().delete());
        } catch (IOException e) {
            throw new IllegalStateException("Can't delete directory", e);
        }
    }

    /**
     * Converts the provided name to module name.
     * <p>
     * Example:
     * <p>
     * my-great-module -> my.great.module
     * my.great-module -> my.great.module
     * MyModule -> mymodule
     *
     * @param name
     * @return
     */
    public static String asModuleName(String name) {
        Objects.requireNonNull(name);
        return name.replaceAll("-", ".").replaceAll("_", ".").toLowerCase();
    }

}
