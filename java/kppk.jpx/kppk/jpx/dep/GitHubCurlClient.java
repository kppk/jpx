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
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This has to be here, cause SubstrateVM doesn't support HTTPS yet.
 */
final class GitHubCurlClient {

    public static List<String> getTags(String user, String project) {
        String url = String.format("https://api.github.com/repos/%s/%s/tags", user, project);
        JSONDocument json = Curl.getAsJson(url);
        return parseTags(json);
    }

    public static Manifest getManifest(String user, String project, String version) {
        String url = String.format("https://raw.githubusercontent.com/%s/%s/%s/%s", user, project, version, Manifest.NAME);
        try {
            String txt = Curl.getAsString(url);
            return Manifest.readFrom(new URI(url), txt);
        } catch (URISyntaxException e) {
            throw new IllegalStateException(e);
        }
    }

    public static void fetch(String user, String project, String version, Path targetDir) {
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
