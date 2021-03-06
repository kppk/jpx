package kppk.jpx.project;

import kppk.jpx.docker.ImageBuilder;
import kppk.jpx.jdk.JdkInstaller;
import kppk.jpx.model.Manifest;
import kppk.jpx.model.Pack;
import kppk.jpx.module.ModuleDescriptor;
import kppk.jpx.module.ModuleDescriptorReader;
import kppk.jpx.sys.ConsolePrinter;
import kppk.jpx.sys.Executor;
import kppk.jpx.sys.SysCommand;
import kppk.jpx.util.IOUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Java project
 */
public final class JavaProject {

    static final String DIR_TARGET = "target";
    static final String DIR_MOD = "mod";
    public static final String DIR_SRC = "java";
    static final String DIR_LIB = "lib";
    static final String DIR_BIN = "binary";
    static final String DIR_DOCKER = "docker";

    private final Path targetDir;
    public final Path baseDir;
    final Path targetModDir;
    final Path srcDir;
    final Path binTargetDir;
    final Path targetDocker;

    public final Manifest manifest;
    public final Path javaHome;
    final Path libDir;
    final Pack.Name name;
    final String binaryName;
    final String mainClass;


    private JavaProject(Manifest manifest,
                        Pack.Name name,
                        Path baseDir,
                        Path javaHome) {
        this.baseDir = baseDir;
        this.name = name;
        this.manifest = manifest;
        this.libDir = baseDir.resolve(DIR_LIB);
        this.javaHome = javaHome;
        this.targetDir = baseDir.resolve(DIR_TARGET);
        this.targetModDir = targetDir.resolve(DIR_MOD);
        this.binTargetDir = targetDir.resolve(DIR_BIN);
        this.srcDir = baseDir.resolve(DIR_SRC);
        this.binaryName = asBinaryName(name);
        this.mainClass = "Main";
        this.targetDocker = targetDir.resolve(DIR_DOCKER);
    }

    public static JavaProject createNew(Manifest mf) {
        Objects.requireNonNull(mf);

        // todo: validate pack repo
        // todo: validate basedir
        Pack.Name name = mf.pack.name;
        Path baseDir = Paths.get(mf.basedir);
        Path javaHome = JdkInstaller.getJavaHomeOrInstall(mf.pack.javaRelease);

        return new JavaProject(mf, name, baseDir, javaHome);
    }

    /**
     * Returns list of directories, which are java modules
     */
    List<String> getModuleDirs() {
        try {
            Stream<Path> projectMods = Stream.of(srcDir);

            Stream<Path> libMods = Stream.empty();
            if (Files.exists(libDir)) {
                libMods = Files.walk(libDir, 2)
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

    public JavaProject build() {

        List<SysCommand> cmds = new ArrayList<>();
        cmds.add(Compiler.getCompiler().compile(this));
        if (!isLibrary()) {
            IOUtil.removeDir(binTargetDir);
            cmds.add(Linker.getLinker().link(this));
            // dump cds data
            cmds.add(SysCommand.builder(binTargetDir.resolve("bin").resolve("java").toString())
                    .addParameter("-Xshare:dump")
                    .build());
        }
        Executor.execute(this, cmds);
        if (!isLibrary()) {
            String launcher = Launcher.getLauncher().create(this);
            Path script = binTargetDir.resolve("bin").resolve(asBinaryName(name));
            try {
                Files.write(script, launcher.getBytes());
                Files.setPosixFilePermissions(script, PosixFilePermissions.fromString("rwxr-xr-x"));
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }

        }
        return this;
    }

    public JavaProject clean() {
        IOUtil.removeDir(targetDir);
        return this;
    }

    public JavaProject install() {
        String dirs = name.toString();
        Path executable = binTargetDir.relativize(binTargetDir.resolve("bin").resolve(asBinaryName(name)));
        new Installer().installBinary(dirs,
                manifest.pack.version.toString(),
                binTargetDir,
                executable);
        return this;
    }

    public JavaProject buildDockerImage(boolean minimize) {
        if (isLibrary()) {
            ConsolePrinter.error(() -> "Project is library, skipping docker image build");
            return this;
        }

        ImageBuilder.buildImage(this.baseDir,
                targetDocker,
                manifest.pack.javaRelease,
                null, //TODO: parametrize
                manifest.pack.version.toString(),
                asBinaryName(name),
                minimize
        );

        return this;

    }

    public boolean isLibrary() {
        return manifest.pack.type == Pack.Type.LIBRARY;
    }

    /**
     * Reads module-info.java as ModuleDescriptor.
     */
    public ModuleDescriptor getModuleDescriptor() {
        return ModuleDescriptorReader.readFrom(baseDir.resolve(asModuleInfoPath(name)));
    }

    /**
     * Returns sub-path to the module-info.java
     */
    public static Path asModuleInfoPath(Pack.Name name) {
        return Paths.get(DIR_SRC, asModuleName(name), "module-info.java");
    }

    /**
     * Returns sub-path to the module-info.java
     */
    public static Path asModuleInfoPath(String org, String repo) {
        return Paths.get(DIR_SRC, org + "." + repo, "module-info.java");
    }

    public static String asModuleName(Pack.Name name) {
        Objects.requireNonNull(name);

        return name.org + "." + name.repo;
    }

    public static String asBinaryName(Pack.Name name) {
        Objects.requireNonNull(name);

        return name.org + "-" + name.repo;
    }

}
