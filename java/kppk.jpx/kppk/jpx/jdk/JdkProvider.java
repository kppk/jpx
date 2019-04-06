package kppk.jpx.jdk;

/**
 * Implementations are used to install java jdk.
 */
interface JdkProvider<T extends JdkRelease> {

    /**
     * Get latest Java version, eg. 12
     *
     * @return java version as String
     */
    String getLatestVersion();


    /**
     * Get latest java release, example: jdk-11.0.1+13
     *
     * @param version java version to get the release for, eg. 12
     * @return java release as String
     */
    T getLatestRelease(String version);


    /**
     * Installs the requested {@link JdkRelease}. Use {@link #getLatestRelease(String)} to get the latest release.
     *
     * @param release Jdk release to install, can't be null.
     */
    void install(T release);


}
