package kppk.jpx.jdk;

import kppk.jpx.config.JPXConfig;

import java.nio.file.Path;

/**
 * Class to represent Jdk release. Version is java version (eg. 10, 11, 12...), name is java release
 * (12+33 or similar, depends on JdkProvider).
 */
class JdkRelease {

    // jdk version
    public final String version;

    // release name
    public final String name;

    JdkRelease(String version, String name) {
        this.version = version;
        this.name = name;
    }

    Path home() {
        switch (Os.TYPE) {
            case mac:
                return JPXConfig.INSTANCE.jdkDir.resolve(version)
                        .resolve(name)
                        .resolve("Contents")
                        .resolve("Home");
            case linux:
                return JPXConfig.INSTANCE.jdkDir.resolve(version)
                        .resolve(name);
            default:
                throw new IllegalStateException("Unsupported OS, currently supported: mac, linux");
        }
    }

//    public static void validate(String release) {
//        try {
//            int num = Integer.parseInt(release);
//            if (num < 9) {
//                throw new IllegalArgumentException("Invalid Java release number, supported values: 9,10,11,...");
//            }
//        } catch (NumberFormatException e) {
//            throw new IllegalArgumentException("Invalid Java release number, supported values: 9,10,11,...");
//        }
//    }

}
