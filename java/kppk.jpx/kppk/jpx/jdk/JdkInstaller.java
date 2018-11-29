package kppk.jpx.jdk;

import kppk.jpx.config.JPXConfig;
import kppk.jpx.json.JSONDocument;
import kppk.jpx.json.JSONFactory;
import kppk.jpx.json.JSONReader;
import kppk.jpx.json.JSONWriter;
import kppk.jpx.sys.Curl;
import kppk.jpx.sys.Executor;
import kppk.jpx.sys.SysCommand;
import kppk.jpx.util.Types;

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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * TODO: Document this
 */
public class JdkInstaller {

    private static final String JDK_DEF_URL = "https://raw.githubusercontent.com/kppk/jpx/master/data/AdoptOpenJDK.json";
    private static final Duration DEFAULT_EXPIRATION = Duration.ofDays(1);

    private static final Supplier<JSONDocument> jdkCatalogSupplier = new CatalogSupplier();

    private JdkInstaller() {
    }

    private static final class Release {
        private final String number;
        private final ZonedDateTime updated;

        public Release(String number, ZonedDateTime updated) {
            this.number = number;
            this.updated = updated;
        }
    }

    private static final class JdkDistro {
        private final String url;
        private final String name;
        private final ZonedDateTime updated;

        private JdkDistro(String url, String name, ZonedDateTime updated) {
            this.url = url;
            this.name = name;
            this.updated = updated;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            JdkDistro that = (JdkDistro) o;
            return url.equals(that.url) &&
                    name.equals(that.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(url, name);
        }
    }

    private static String getLatestJavaRelease() {
        Release release = readLatestRelease();
        if (release == null || expired(release.updated)) {
            String number = jdkCatalogSupplier.get().getString("latest");
            release = new Release(number, null);
            writeLatestRelease(release);
        }
        return release.number;
    }

    private static JdkDistro getLatestGithubRelease(String releaseUrl) {
        JSONDocument releaseJson = Curl.getAsJson(releaseUrl);

        String releaseName = releaseJson.getString("tag_name");
        List<Object> assets = releaseJson.getList("assets");
        for (Object asset : assets) {
            JSONDocument assetJson = Types.safeCast(asset, JSONDocument.class);
            String name = assetJson.getString("name");
            if (name.contains(releaseFileName()) && name.endsWith(".tar.gz")) {
                // found the release to download
                String url = assetJson.getString("browser_download_url");
                return new JdkDistro(url, releaseName, ZonedDateTime.now());
            }
        }
        throw new IllegalStateException("Unable to find the JDK to download");
    }

    private static String releaseFileName() {
        switch (Os.TYPE) {
            case mac:
                return "-jdk_x64_mac_hotspot_";
            case linux:
                return "-jdk_x64_linux_hotspot_";
            default:
                throw new IllegalStateException("Unsupported OS, currently supported: mac, linux");
        }
    }

    public static JdkDistro install(String javaRelease) {
        if (javaRelease == null) {
            javaRelease = getLatestJavaRelease();
        }
        String releaseUrl = jdkCatalogSupplier.get().getString(javaRelease);
        if (releaseUrl == null) {
            throw new IllegalArgumentException("Invalid java release provided [" + javaRelease + "]");
        }
        JdkDistro jdkDistro = getLatestGithubRelease(releaseUrl);

        if (isLatestBuildAvailable(javaRelease, jdkDistro)) {
            // no need to download, we have the latest build
            return jdkDistro;
        }

        Path targetDir = JPXConfig.INSTANCE.jdkDir.resolve(javaRelease);
        Path targetReleaseDir = JPXConfig.INSTANCE.jdkDir.resolve(javaRelease).resolve(jdkDistro.name);
        if (!Files.exists(targetReleaseDir)) {
            try {
                Files.createDirectories(targetDir);
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }

        Path downloaded = Curl.get(jdkDistro.url);
        SysCommand untar = SysCommand.builder("tar")
                .addParameter("-xf")
                .addParameter(downloaded.toAbsolutePath().toString())
                .addParameter("-C")
                .addParameter(targetDir.toAbsolutePath().toString())
                .build();
        Executor.execute(null, targetDir, Collections.singletonList(untar));

        writeLatestBuild(javaRelease, jdkDistro);
        return jdkDistro;
    }

    public static Path getJavaHomeOrInstall(String javaRelease) {
        if (javaRelease == null) {
            javaRelease = getLatestJavaRelease();
        }
        JdkDistro latest = readLatestBuild(javaRelease);
        if (latest == null || expired(latest.updated)) {
            latest = install(javaRelease);
        }
        switch (Os.TYPE) {
            case mac:
                return JPXConfig.INSTANCE.jdkDir.resolve(javaRelease)
                        .resolve(latest.name)
                        .resolve("Contents")
                        .resolve("Home");
            case linux:
                return JPXConfig.INSTANCE.jdkDir.resolve(javaRelease)
                        .resolve(latest.name);
            default:
                throw new IllegalStateException("Unsupported OS, currently supported: mac, linux");
        }
    }

    private static boolean isLatestBuildAvailable(String javaRelease, JdkDistro jdkDistro) {
        JdkDistro latest = readLatestBuild(javaRelease);
        if (latest == null) {
            return false;
        }
        return latest.equals(jdkDistro);
    }

    private static JdkDistro readLatestBuild(String javaRelease) {
        Path latestFile = latestBuildFile(javaRelease);
        if (!Files.exists(latestFile)) {
            return null;
        }
        try (Reader r = new BufferedReader(new FileReader(latestFile.toFile()))) {
            JSONReader reader = JSONFactory.instance().makeReader(r);
            JSONDocument latestJson = reader.build();
            String name = latestJson.getString("name");
            String url = latestJson.getString("url");
            ZonedDateTime updated = ZonedDateTime.parse(latestJson.getString("updated"));
            return new JdkDistro(url, name, updated);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private static void writeLatestBuild(String javaRelease, JdkDistro jdkDistro) {
        Path latestFile = latestBuildFile(javaRelease);
        try (Writer w = new BufferedWriter(new FileWriter(latestFile.toFile()))) {
            Map<String, Object> latest = new HashMap<>();
            latest.put("name", jdkDistro.name);
            latest.put("url", jdkDistro.url);
            latest.put("updated", ZonedDateTime.now().toString());
            JSONWriter jsonWriter = JSONFactory.instance().makeWriter(w);
            jsonWriter.writeObject(latest);
            jsonWriter.flush();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private static Path latestBuildFile(String javaRelease) {
        return JPXConfig.INSTANCE.jdkDir.resolve(javaRelease).resolve("latest");
    }

    private static Release readLatestRelease() {
        Path latestFile = latestReleaseFile();
        if (!Files.exists(latestFile)) {
            return null;
        }
        try (Reader r = new BufferedReader(new FileReader(latestFile.toFile()))) {
            JSONReader reader = JSONFactory.instance().makeReader(r);
            JSONDocument latestJson = reader.build();
            String number = latestJson.getString("number");
            ZonedDateTime updated = ZonedDateTime.parse(latestJson.getString("updated"));
            return new Release(number, updated);

        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private static void writeLatestRelease(Release release) {
        Path latestFile = latestReleaseFile();
        try (Writer w = new BufferedWriter(new FileWriter(latestFile.toFile()))) {
            Map<String, Object> latest = new HashMap<>();
            latest.put("number", release.number);
            latest.put("updated", ZonedDateTime.now().toString());
            JSONWriter jsonWriter = JSONFactory.instance().makeWriter(w);
            jsonWriter.writeObject(latest);
            jsonWriter.flush();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private static Path latestReleaseFile() {
        return JPXConfig.INSTANCE.jdkDir.resolve("latest");

    }

    private static boolean expired(ZonedDateTime dateTime) {
        Duration diff = Duration.between(dateTime, ZonedDateTime.now());
        return DEFAULT_EXPIRATION.compareTo(diff) < 1;
    }

    private static final class CatalogSupplier implements Supplier<JSONDocument> {

        private JSONDocument catalog;

        @Override
        public JSONDocument get() {
            // don't care about locking
            if (catalog == null) {
                catalog = Curl.getAsJson(JDK_DEF_URL);
            }
            return catalog;
        }
    }

}
