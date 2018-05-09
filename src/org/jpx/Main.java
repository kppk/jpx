package org.jpx;

import org.jpx.cli.App;
import org.jpx.cli.StringFlag;
import org.jpx.cmd.Project;

public class Main {

    public static void main(String[] args) {
        try {
            new Main().run(args);
        } catch (Exception e) {
            System.err.println("ERROR: " + e.getMessage());
            System.exit(1);
        }
    }

    private void run(String[] args) {
        App.builder()
                .setName("jpx")
                .setUsage("Java Packs manager")
                .addCommand(Project.CMD_NEW)
                .addCommand(Project.CMD_INIT)
                .addFlag(StringFlag.builder()
                        .setName("verbose")
                        .setShortName("v")
                        .setUsage("Use verbose output")
                        .build())
                .build()
                .execute(args);

    }


    /**
     * Support the following commands:
     *
     * new <dir> -- create new directory and run init there
     * init -- creates new jpx.toml file in current directory
     * build -- build current project to target directory
     * clean -- removes the target directory
     *
     */
}
