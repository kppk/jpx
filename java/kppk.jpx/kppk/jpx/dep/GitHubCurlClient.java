package kppk.jpx.dep;

import kppk.jpx.json.JSONDocument;
import kppk.jpx.model.Manifest;
import kppk.jpx.sys.Curl;
import kppk.jpx.sys.Executor;
import kppk.jpx.sys.SysCommand;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * We use curl so we don't need to provide ssl libs with the native image we build.
 * <p>
 * see https://github.com/oracle/graal/blob/master/substratevm/JCA-SECURITY-SERVICES.md
 */
final class GitHubCurlClient {

    static List<String> getTags(String user, String project) {
        String url = String.format("https://api.github.com/repos/%s/%s/tags", user, project);
        JSONDocument json = Curl.getAsJson(url);
        return parseTags(json);
    }

    static Manifest getManifest(String user, String project, String version) {
        String url = String.format("https://raw.githubusercontent.com/%s/%s/%s/%s", user, project, version, Manifest.NAME);
        try {
            String txt = Curl.getAsString(url);
            return Manifest.readFrom(new URI(url), txt);
        } catch (URISyntaxException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Gets raw file content from github.
     *
     * @param user    github user/org
     * @param project github project/repo
     * @param version version (label)
     * @param path    relative path to the file
     * @return content of the file as String
     */
    static String getProjecFileRaw(String user, String project, String version, Path path) {
        String base = String.format("https://raw.githubusercontent.com/%s/%s/%s", user, project, version);
        String full = Paths.get(base).resolve(path).toString();
        return Curl.getAsString(full);
    }

    static void fetch(String user, String project, String version, Path targetDir) {
        try {
            Files.createDirectories(targetDir);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        String url = String.format("https://github.com/%s/%s/tarball/%s/", user, project, version);
        Path tarFile = Curl.get(url);
        SysCommand curl = SysCommand.builder("tar")
                .addParameter("-xf")
                .addParameter(tarFile.toAbsolutePath().toString())
                .addParameter("-C")
                .addParameter(targetDir.toAbsolutePath().toString())
                .addParameter("--strip")
                .addParameter("1")
                .build();

        Executor.execute(null, targetDir, Collections.singletonList(curl));
    }

    static List<String> parseTags(JSONDocument doc) {
        if (!doc.isArray()) {
            throw new IllegalStateException("Unexpected GitHub json response, expected array");
        }
        return doc.array().stream()
                .map(o -> {
                    if (!(o instanceof JSONDocument)) {
                        throw new IllegalStateException("Unexpected GitHub json response, expected array of objects");
                    }
                    if (!((JSONDocument) o).isObject()) {
                        throw new IllegalStateException("Unexpected GitHub json response, expected array of objects");
                    }
                    return ((JSONDocument) o).getString("name");
                })
                .collect(Collectors.toList());
    }

}
