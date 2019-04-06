package kppk.jpx.jdk;

import kppk.jpx.config.JPXConfig;
import kppk.jpx.json.JSONDocument;
import kppk.jpx.json.JSONFactory;
import kppk.jpx.json.JSONReader;
import kppk.jpx.json.JSONWriter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Takes care of installing JDK binaries. <br/>
 * <p>
 * Note: 'version' is used for java version (10, 11, 12,...), 'release' is used for java version release (12.0.0+33,...)
 */
public final class JdkInstaller {

    private static final Duration DEFAULT_EXPIRATION = Duration.ofDays(10);

    /**
     * No instances, just functions.
     */
    private JdkInstaller() {
    }

    /**
     * Get the home for the provided java version. eg. 12 <br/>
     * If the version is not provided, it will use the latest version. <br/>
     * If that version is provided, but not available, it will download latest release of that version.<br/>
     * If the version is provided, and available, it will still check if the release is the latest one, and install latest if not the case.
     *
     * @param version java version, eg. 12
     * @return path to java home
     */
    public static Path getJavaHomeOrInstall(String version) {
        AdoptOpenJdkProvider jdkProvider = new AdoptOpenJdkProvider();
        if (version == null) {
            version = getLatestJavaVersion(jdkProvider);
        }

        Path latestFile = JPXConfig.INSTANCE.jdkDir.resolve(version).resolve("latest");
        Latest latest = readLatest(latestFile);
        if (latest == null || latest.isExpired()) {
            // use the jdk provider to get the latest release for the specified java version
            AdoptOpenJdkProvider.JdkRelease release = jdkProvider.getLatestRelease(version);
            if (latest != null && latest.value.equals(release.name)) {
                // same release as we have in latest, no need to install
                return release.home();
            }
            // install
            jdkProvider.install(release);
            writeLatest(latestFile, release.name);
            return release.home();
        }
        return new JdkRelease(version, latest.value).home();
    }

    /**
     * Class to hold the latest data, it has value and timestamp.
     * <p>
     * This is used to throttle the requests to JdkProvider, only when the latest `updated` is expired, new request
     * is gonna be made.
     */
    private static final class Latest {
        private final String value;
        private final ZonedDateTime updated;

        Latest(String value, ZonedDateTime updated) {
            this.value = value;
            this.updated = updated;
        }

        boolean isExpired() {
            Duration diff = Duration.between(updated, ZonedDateTime.now());
            return DEFAULT_EXPIRATION.compareTo(diff) < 1;
        }

    }

    /**
     * Returns latest java version (10, 11, 12, ...). Used only when the java version is not specified in project's
     * toml file.
     *
     * @param jdkProvider JdkProvider implementation to use to get the latest version
     * @return version as a String
     */
    private static String getLatestJavaVersion(JdkProvider<?> jdkProvider) {
        Path latestFile = JPXConfig.INSTANCE.jdkDir.resolve("latest");
        Latest latest = readLatest(latestFile);
        if (latest == null || latest.isExpired()) {
            // use the jdk provider to get the latest java version
            String version = jdkProvider.getLatestVersion();
            writeLatest(latestFile, version);
            return version;
        }
        return latest.value;
    }

    /**
     * Reads latest file (json) and de-serializes it to Latest object.
     *
     * @param latestFile Path to file containing latest info
     * @return Latest object
     */
    private static Latest readLatest(Path latestFile) {
        if (!Files.exists(latestFile)) {
            return null;
        }
        try (Reader r = new BufferedReader(new FileReader(latestFile.toFile()))) {
            JSONReader reader = JSONFactory.instance().makeReader(r);
            JSONDocument latestJson = reader.build();
            String name = latestJson.getString("value");
            ZonedDateTime updated = ZonedDateTime.parse(latestJson.getString("updated"));
            return new Latest(name, updated);

        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Writes latest to provided file. This is used to throttle the requests to JdkProvider.
     *
     * @param latestFile path to store to
     * @param value      latest value to store
     * @see #getJavaHomeOrInstall(String)
     */
    private static void writeLatest(Path latestFile, String value) {
        try {
            Files.createDirectories(latestFile.getParent());
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        try (Writer w = new BufferedWriter(new FileWriter(latestFile.toFile()))) {
            Map<String, Object> latest = new HashMap<>();
            latest.put("value", value);
            latest.put("updated", ZonedDateTime.now().toString());
            JSONWriter jsonWriter = JSONFactory.instance().makeWriter(w);
            jsonWriter.writeObject(latest);
            jsonWriter.flush();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

}
