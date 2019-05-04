package kppk.jpx.cmd;

import kppk.cli.Command;
import kppk.cli.StringFlag;
import kppk.jpx.jdk.JdkInstaller;

import static kppk.jpx.Main.handleCommon;

/**
 * `java' cli command - downloads JDK release.
 */
public final class Java {

    private static final StringFlag FLAG_RELEASE = StringFlag.builder()
            .setName("release")
            .setUsage("Release version, example: 10")
            .build();

    public static final Command CMD_JAVA = Command.builder()
            .setName("java")
            .setUsage("Installs/Updates java development kit")
            .addFlag(FLAG_RELEASE)
            .setExecutor(handleCommon.andThen(ctx -> installJava(ctx.getFlagValue(FLAG_RELEASE))))
            .build();

    private static void installJava(String release) {
        JdkInstaller.getJavaHomeOrInstall(release);
    }


}
