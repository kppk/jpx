package kppk.jpx.sys;

import kppk.jpx.config.JPXConfig;
import kppk.jpx.project.JavaProject;
import kppk.jpx.util.IOUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * TODO: Document this
 */
public final class Executor {

    private Executor() {
    }

    public static void execute(Path binaryDir, Path baseDir, List<SysCommand> commands) {
        if (baseDir == null) {
            baseDir = JPXConfig.INSTANCE.tmpDir;
        }

        try {
            for (SysCommand cmd : commands) {
                List<String> args = cmd.toListWithFullPath(binaryDir);
                ConsolePrinter.verbose(() -> "[Execute] " + args.stream().collect(Collectors.joining(" ")));
                Process process = new ProcessBuilder(args)
                        .directory(baseDir.toFile())
                        .redirectErrorStream(true)
                        .start();
                int code = process.waitFor();
                if (code != 0) {
                    throw new IllegalStateException("SysCommand failed");
                }
            }
        } catch (IOException | InterruptedException e) {
            throw new IllegalStateException(e);
        }
    }


    public static void execute(JavaProject project, List<SysCommand> commands) {
        execute(project.javaHome.resolve("bin"), project.baseDir, commands);
    }

    public static void execute(SysCommand command) {
        execute(null, null, Collections.singletonList(command));
    }

    public static String executeAndReadAll(SysCommand command) {
        try (Reader reader = executeAndRead(command)) {
            return IOUtil.readAll(reader);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public static Reader executeAndRead(SysCommand command) {
        File baseDir = JPXConfig.INSTANCE.tmpDir.toFile();
        List<String> args = command.toListWithFullPath(null);
        ConsolePrinter.verbose(() -> "[Execute] " + String.join(" ", args));
        try {
            Process process = new ProcessBuilder(args)
                    .directory(baseDir)
                    .redirectErrorStream(true)
                    .start();
            int code = process.waitFor();
            if (code != 0) {
                throw new IllegalStateException("SysCommand failed");
            }
            return new BufferedReader(new InputStreamReader(process.getInputStream()));
        } catch (IOException | InterruptedException e) {
            throw new IllegalStateException(e);
        }
    }

    // TODO: docker executor
}
