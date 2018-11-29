package kppk.jpx.cmd;

import kppk.jpx.cli.Command;
import kppk.jpx.cli.StringFlag;
import kppk.jpx.jdk.JdkInstaller;

/**
 * TODO: Document this
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
            .setExecutor(ctx -> JdkInstaller.install(
                    ctx.getFlagValue(FLAG_RELEASE)
            ))
            .build();


}
