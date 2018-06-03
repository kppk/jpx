package org.jpx;

import org.jpx.cli.App;
import org.jpx.cli.BooleanFlag;
import org.jpx.cli.Context;
import org.jpx.cli.StringFlag;
import org.jpx.cmd.Build;
import org.jpx.cmd.Dep;
import org.jpx.cmd.Setup;
import org.jpx.sys.ConsolePrinter;

import java.util.function.Consumer;

public class Main {

    public static final BooleanFlag FLAG_VERBOSE = BooleanFlag.builder()
            .setName("verbose")
            .setShortName("v")
            .setUsage("Use verbose output")
            .setDefaultValue(Boolean.FALSE)
            .build();

    public static final Consumer<Context> handleCommon = (ctx) -> {
        Boolean verbose = ctx.getFlagValue(FLAG_VERBOSE);
        if (verbose) {
            ConsolePrinter.level = ConsolePrinter.Verbosity.VERBOSE;
        }
    };

    public static void main(String[] args) {
        try {
            new Main().run(args);
        } catch (Exception e) {
            ConsolePrinter.error(e);
            System.exit(1);
        }
    }

    private void run(String[] args) {
        App.builder()
                .setName("jpx")
                .setUsage("Java Packs manager")
                .addCommand(Setup.CMD_NEW)
                .addCommand(Setup.CMD_INIT)
                .addCommand(Build.CMD_BUILD)
                .addCommand(Build.CMD_CLEAN)
                .addCommand(Dep.CMD_DEP_INSTALL)
                .addFlag(FLAG_VERBOSE)
                .build()
                .execute(args);

    }


}
