package kppk.jpx.project;

import kppk.jpx.sys.SysCommand;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * TODO: Document this
 */
interface Compiler {

    SysCommand compile(JavaProject project);

    static Compiler getCompiler(JDK jdk) {
        Objects.requireNonNull(jdk);
        switch (jdk) {
            case v9:
            case v10:
                return JAVA_9;
        }
        throw new IllegalArgumentException("Unsupported JDK: " + jdk.release);
    }

    Compiler JAVA_9 = project -> {
        try {
            Files.createDirectories(project.targetModDir);
            String mods = project.getModuleDirs().stream().collect(Collectors.joining(File.pathSeparator));
            String module = JavaProject.asModuleName(project.name);
            SysCommand.Builder builder = SysCommand.builder("javac")
                    .addParameter("-d")
                    .addParameter(project.targetModDir.toString())
                    .addParameter("--module-source-path")
                    .addParameter(mods)
                    .addParameter("--module")
                    .addParameter(module);
            return builder.build();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    };


}
