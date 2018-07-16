package kppk.jpx.cli;

/**
 * TODO: Document this
 */
public class AppTest {

    public static void main(String[] args) {

        AppTest appTest = new AppTest();
        appTest.run(new String[]{"new", "dir"});


    }

    private void run(String[] args) {
        App.builder()
                .setName("jpx")
                .setUsage("Java Packs manager")
                .addCommand(Command.builder()
                        .setName("new")
                        .setUsage("Create new project")
                        .setArg(StringFlag.builder()
                                .setName("DIR")
                                .build())
                        .setExecutor(this::initProject)
                        .build())
                .addCommand(Command.builder()
                        .setName("init")
                        .setUsage("Create new project in current directory")
                        .setExecutor(this::newProject)
                        .build())
                .addFlag(StringFlag.builder()
                        .setName("verbose")
                        .setShortName("v")
                        .build())
                .build()
                .execute(args);

    }

    private void initProject(Context context) {
        System.out.println("---INIT---");
        System.out.println(context);
    }

    private void newProject(Context context) {
        System.out.println("---NEW---");
        System.out.println(context);
    }

}
