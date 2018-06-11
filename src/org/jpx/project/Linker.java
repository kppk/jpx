package org.jpx.project;

import org.jpx.sys.SysCommand;

import java.util.Objects;

/**
 * TODO: Document this
 */
@FunctionalInterface
public interface Linker {

    SysCommand link(JavaProject project);

    static Linker getLinker(JDK jdk) {
        Objects.requireNonNull(jdk);
        switch (jdk) {
            case v9:
            case v10:
                return JAVA_9;
        }
        throw new IllegalArgumentException("Unsupported JDK: " + jdk.release);
    }

    Linker JAVA_9 = project -> {
        if (project.isLibrary()) {
            // TODO: handle this
        }
        String module = JavaProject.asModuleName(project.name);
        return SysCommand.builder("jlink")
                .addParameter("--module-path")
                .addParameter(project.targetModDir.toString() + ":" + project.javaHome.resolve("jmods"))
                .addParameter("--add-modules")
                .addParameter(module)
                .addParameter("--output")
                .addParameter(project.binTargetDir.toString())
                .addParameter("--launcher")
                .addParameter(String.format("%s=%s/%s.%s",
                        JavaProject.asBinaryName(project.name),
                        module,
                        module,
                        project.mainClass))
                .build();
    };

}
