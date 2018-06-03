package org.jpx.project;

import org.jpx.model.Manifest;
import org.jpx.model.Pack;
import org.jpx.sys.Executor;
import org.jpx.sys.SysCommand;
import org.jpx.util.IOUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * TODO: Document this
 */
public final class JavaProject {

    static final String DIR_TARGET = "target";
    static final String DIR_MOD = "mod";
    public static final String DIR_SRC = "java";
    static final String DIR_LIB = "lib";
    static final String DIR_BIN = "binary";


    private final Path targetDir;
    public final Path baseDir;
    final Path targetModDir;
    final Path srcDir;
    final Path binTargetDir;
    final Manifest manifest;
    final JDK jdk;
    public final Path javaHome;
    final Path libDir;
    final String name;
    final String binaryName;
    final String mainClass;

    private JavaProject(Manifest manifest,
                        String name,
                        Path baseDir,
                        JDK jdk,
                        Path javaHome) {
        this.baseDir = baseDir;
        this.name = name;
        this.manifest = manifest;
        this.libDir = baseDir.resolve(DIR_LIB);
        this.jdk = jdk;
        this.javaHome = javaHome;
        this.targetDir = baseDir.resolve(DIR_TARGET);
        this.targetModDir = targetDir.resolve(DIR_MOD);
        this.binTargetDir = targetDir.resolve(DIR_BIN);
        this.srcDir = baseDir.resolve(DIR_SRC);
        this.binaryName = asBinaryName(name);
        this.mainClass = "Main";
    }

    public static JavaProject createNew(Manifest mf) {
        Objects.requireNonNull(mf);

        // todo: validate pack name
        // todo: validate basedir
        String name = mf.pack.name;
        Path baseDir = Paths.get(mf.basedir);
        JDK jdk = JDK.releaseOf(mf.pack.javaRelease);
        Path javaHome = JavaHomeSupplier.getJavaHomeSupplier(jdk).get();

        return new JavaProject(mf, name, baseDir, jdk, javaHome);
    }

    List<String> getModuleDirs() {
        try {
            Stream<Path> projectMods = Stream.of(srcDir);

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

    public JavaProject build(boolean link) {

        List<SysCommand> cmds = new ArrayList<>();
        cmds.add(Compiler.getCompiler(jdk).compile(this));
        if (link && !isLibrary()) {
            IOUtil.removeDir(binTargetDir);
            cmds.add(Linker.getLinker(jdk).link(this));
        }
        Executor.execute(this, cmds);
        return this;
    }

    public JavaProject clean() {
        IOUtil.removeDir(targetDir);
        return this;
    }

    public JavaProject install() {
        String dirs = name.replace(".", "/");
        Path executable = binTargetDir.relativize(binTargetDir.resolve("bin").resolve(asBinaryName(name)));
        new Installer().installBinary(dirs,
                manifest.pack.version.toString(),
                binTargetDir,
                executable);
        return this;
    }


    public boolean isLibrary() {
        return manifest.pack.type == Pack.Type.LIBRARY;
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

    public static String asBinaryName(String name) {
        Objects.requireNonNull(name);
        return name.replace(".", "-");
    }

}
