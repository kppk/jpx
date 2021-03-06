package kppk.jpx.project;

import kppk.jpx.sys.SysCommand;

/**
 * TODO: Document this
 */
@FunctionalInterface
public interface Linker {

    SysCommand link(JavaProject project);

    static Linker getLinker() {
        return JAVA_9;
    }

    Linker JAVA_9 = (project) -> {
        String module = JavaProject.asModuleName(project.name);
        return SysCommand.builder("jlink")
                .addParameter("--module-path")
                .addParameter(project.targetModDir.toString() + ":" + project.javaHome.resolve("jmods"))
                .addParameter("--add-modules")
                .addParameter(module)
                .addParameter("--output")
                .addParameter(project.binTargetDir.toString()).build();
    };

}
