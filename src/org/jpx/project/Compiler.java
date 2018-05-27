package org.jpx.project;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * TODO: Document this
 */
interface Compiler {

    void compile(JavaProject project);

    static Compiler getCompiler(JavaProject.JDK jdk) {
        Objects.requireNonNull(jdk);
        switch (jdk) {
            case REL_8:
                return COMPILER_JAVA_8;
            case REL_9:
            case REL_10:
                return COMPILER_JAVA_9;
        }
        throw new IllegalArgumentException("Unsupported JDK: " + jdk.release);
    }

    Compiler COMPILER_JAVA_8 = project -> {
        try {
            Files.createDirectories(project.classesDir);
            String javac = project.javaHome.resolve("bin").resolve("javac").toString();
            List<String> args = new ArrayList<>();
            args.addAll(Arrays.asList(javac,
                    "-d",
                    project.classesDir.toString(),
                    "-sourcepath",
                    project.srcDir.toString()
            ));
            args.addAll(project.getSourceFiles());
            Process p = new ProcessBuilder()
                    .inheritIO()
                    .command(args)
                    .directory(project.baseDir.toFile())
                    .redirectErrorStream(true)
                    .start();
            p.waitFor();
        } catch (IOException | InterruptedException e) {
            throw new IllegalStateException(e);
        }
    };

    Compiler COMPILER_JAVA_9 = project -> {
        try {
            Files.createDirectories(project.modTargetDir);
            String javac = project.javaHome.resolve("bin").resolve("javac").toString();
            String mods = project.getModuleDirs().stream().collect(Collectors.joining(File.pathSeparator));
            List<String> args = new ArrayList<>();
            args.addAll(Arrays.asList(javac,
                    "-d",
                    project.modTargetDir.toString(),
                    "--module-source-path",
                    mods,
                    "--module",
                    project.name
            ));
            args.addAll(project.getSourceFiles());
            Process p = new ProcessBuilder()
                    .inheritIO()
                    .command(args)
                    .directory(project.baseDir.toFile())
                    .redirectErrorStream(true)
                    .start();
            p.waitFor();
        } catch (IOException | InterruptedException e) {
            throw new IllegalStateException(e);
        }
    };


}
