package kppk.jpx.jdk;

import kppk.jpx.config.JPXConfig;
import kppk.jpx.json.JSONDocument;
import kppk.jpx.sys.ConsolePrinter;
import kppk.jpx.sys.Curl;
import kppk.jpx.sys.Executor;
import kppk.jpx.sys.SysCommand;
import kppk.jpx.util.IOUtil;
import kppk.jpx.util.Types;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Objects;


/**
 * JdkProvider that uses https://www.adoptopenjdk.net to download jdk.
 */
final class AdoptOpenJdkProvider implements JdkProvider<AdoptOpenJdkProvider.JdkRelease> {

    private static final String LATEST_RELEASE = "https://api.adoptopenjdk.net/v2/info/releases/openjdk%s?release=latest&type=jdk&openjdk_impl=hotspot&os=%s&arch=%s";

    static final class JdkRelease extends kppk.jpx.jdk.JdkRelease {

        final String binaryLink;
        final String checksum_link;

        JdkRelease(String version, String name, String binaryLink, String checksum_link) {
            super(version, name);
            this.binaryLink = binaryLink;
            this.checksum_link = checksum_link;
        }

    }

    @Override
    public String getLatestVersion() {
        // todo: couldn't find good way to get latest java version, hardcoded for now
        return "12";
    }

    @Override
    public JdkRelease getLatestRelease(String version) {
        Objects.requireNonNull(version);
        String url = String.format(LATEST_RELEASE, version, Os.TYPE, Os.ARCH);
        JSONDocument releaseJson = Curl.getAsJson(url);
        String name = releaseJson.getString("release_name");
        List<Object> bins = releaseJson.getList("binaries");
        if (bins.size() != 1) {
            throw new IllegalStateException("Can't get latest release info from https://www.adoptopenjdk.net, expected 1 binary info, got " + bins.size());
        }
        JSONDocument bin = Types.safeCast(bins.get(0), JSONDocument.class);
        String binaryLink = bin.getString("binary_link");
        String checksumLink = bin.getString("checksum_link");

        return new JdkRelease(version, name, binaryLink, checksumLink);
    }

    @Override
    public void install(JdkRelease release) {
        Objects.requireNonNull(release);

        Path targetDir = JPXConfig.INSTANCE.jdkDir.resolve(release.version);
        Path targetReleaseDir = JPXConfig.INSTANCE.jdkDir.resolve(release.version).resolve(release.name);
        if (!Files.exists(targetReleaseDir)) {
            try {
                Files.createDirectories(targetDir);
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }

        ConsolePrinter.infoWithProgress(() -> String.format("Downloading Java %s [%s]", release.version, release.name),
                () -> {
                    Path downloaded = Curl.get(release.binaryLink);
                    String checksum = Curl.getAsString(release.checksum_link);
                    String downloadedChecksum = IOUtil.sha256file(downloaded);
                    if (!validChecksum(downloadedChecksum, checksum)) {
                        throw new IllegalStateException("Checksum of downloaded java archive doesn't match, try again");
                    }

                    SysCommand untar = SysCommand.builder("tar")
                            .addParameter("-xf")
                            .addParameter(downloaded.toAbsolutePath().toString())
                            .addParameter("-C")
                            .addParameter(targetDir.toAbsolutePath().toString())
                            .build();
                    Executor.execute(null, targetDir, Collections.singletonList(untar));
                });

    }

    private static boolean validChecksum(String checksum1, String checksum2) {
        Objects.requireNonNull(checksum1);
        Objects.requireNonNull(checksum2);
        String chk1 = checksum1.split(" ")[0];
        String chk2 = checksum2.split(" ")[0];
        return chk1.equalsIgnoreCase(chk2);
    }
}
