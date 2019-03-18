package kppk.jpx.project;

import kppk.jpx.sys.SysCommand;

/**
 * TODO: Document this
 */
@FunctionalInterface
public interface Linker {

    SysCommand link(JavaProject project, boolean minimize);

    static Linker getLinker() {
        return JAVA_9;
    }

    Linker JAVA_9 = (project, minimize) -> {
        if (project.isLibrary()) {
            // TODO: handle this
        }
        String module = JavaProject.asModuleName(project.name);
        SysCommand.Builder jlink =  SysCommand.builder("jlink")
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
                        project.mainClass));

        if (minimize) {
            jlink.addParameter("--strip-debug");
        }
        return jlink.build();
    };

}
