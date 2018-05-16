package org.jpx.project;

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
public final class JavaProject {

    private static final String DIR_TARGET = "target";
    private static final String DIR_CLASSES = "classes";
    private static final String DIR_DOC = "doc";
    private static final String DIR_SRC = "src";

    private final Path srcDir;
    private final Path targetDir;
    private final Path classesDir;
    private final Path baseDir;
    private final Path docDir;
    private final Manifest manifest;
    private JDK jdk;

    public JavaProject(Manifest mf) {
        this.manifest = Objects.requireNonNull(mf);
        this.baseDir = mf.basedir;
        srcDir = baseDir.resolve(DIR_SRC);
        targetDir = baseDir.resolve(DIR_TARGET);
        classesDir = targetDir.resolve(DIR_CLASSES);
        docDir = targetDir.resolve(DIR_DOC);
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
            String javac = getJavaHome().resolve("bin").resolve("javac").toString();
            List<String> args = new ArrayList<>();
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

    public JavaProject doc() {
        try {
            String javadoc = getJavaHome().resolve("bin").resolve("javadoc").toString();
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
            String jar = getJavaHome().resolve("bin").resolve("jar").toString();
            Process p = new ProcessBuilder()
                    //.inheritIO()
                    .command(jar,
                            "cfv",
                            targetDir.resolve(manifest.pack.name + ".jar").toString(),
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

}
