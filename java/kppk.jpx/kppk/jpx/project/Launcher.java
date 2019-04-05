package kppk.jpx.project;

/**
 * Creates launcher script for binaries
 */
@FunctionalInterface
public interface Launcher {

    String create(JavaProject project);

    static Launcher getLauncher() {
        return JAVA_9;
    }

    Launcher JAVA_9 = (project) ->
            "#!/bin/sh\n" +
                    "JLINK_VM_OPTIONS=-Xshare:auto\n" +
                    "DIR=`dirname $0`\n" +
                    "$DIR/java $JLINK_VM_OPTIONS -m " + String.format("%s/%s.%s",
                    JavaProject.asModuleName(project.name),
                    JavaProject.asModuleName(project.name),
                    project.mainClass) + " $@";

}
