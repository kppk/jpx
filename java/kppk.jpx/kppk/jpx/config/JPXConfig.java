package kppk.jpx.config;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * TODO: Document this
 */
public class JPXConfig {

    private static final String HOME_DIR_NAME = ".jpx";

    public final Path home;

    public static final JPXConfig INSTANCE = getInstance();


    private static JPXConfig getInstance() {
        Path jpxHome = Paths.get(System.getProperty("user.home")).resolve(HOME_DIR_NAME);
        return new JPXConfig(jpxHome);
    }

    private JPXConfig(Path home) {
        this.home = home;
    }


}
