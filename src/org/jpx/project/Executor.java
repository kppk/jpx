package org.jpx.project;

import java.io.IOException;
import java.util.List;

/**
 * TODO: Document this
 */
public interface Executor {


    void execute(JavaProject project, List<Command> commands);

    Executor LOCAL = (project, commands) -> {
        try {
            for (Command cmd : commands) {
                List<String> args = cmd.toListWithFullPath(project.javaHome.resolve("bin"));
                Process process = new ProcessBuilder(args)
                        .directory(project.baseDir.toFile())
                        .inheritIO()
                        .redirectErrorStream(true)
                        .start();
                int code = process.waitFor();
                if (code != 0) {
                    throw new IllegalStateException("Command failed");
                }
            }
        } catch (IOException | InterruptedException e) {
            throw new IllegalStateException(e);
        }
    };

    Executor DOCKER = (project, commands) -> {

    };
}
