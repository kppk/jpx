package kppk.jpx.config;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * TODO: Document this
 */
public class JPXConfig {

    private static final String HOME_DIR_NAME = ".jpx";
    private static final String JDK_DIR_NAME = "java";
    private static final String TMP_DIR_NAME = ".tmp";

    public final Path home;
    public final Path jdkDir;
    public final Path tmpDir;

    public static final JPXConfig INSTANCE = getInstance();


    private static JPXConfig getInstance() {
        Path jpxHome = Paths.get(System.getProperty("user.home")).resolve(HOME_DIR_NAME);
        return new JPXConfig(jpxHome);
    }

    private JPXConfig(Path home) {
        this.home = home;
        this.jdkDir = home.resolve(JDK_DIR_NAME);
        this.tmpDir = home.resolve(TMP_DIR_NAME);
    }


}
