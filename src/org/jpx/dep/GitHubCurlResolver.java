package org.jpx.dep;

import org.jpx.json.JSONFactory;
import org.jpx.json.JSONReader;
import org.jpx.model.Manifest;
import org.jpx.sys.Executor;
import org.jpx.sys.SysCommand;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This has to be here, cause SubstrateVM doesn't support HTTPS yet.
 */
public class GitHubCurlResolver {

    public static List<String> getTags(String user, String project) {
        String url = String.format("https://api.github.com/repos/%s/%s/tags", user, project);
        SysCommand curl = SysCommand.builder("curl")
                .addParameter("-s")
                .addParameter(url)
                .build();
        String ret = Executor.executeAndReadLines(curl).stream().collect(Collectors.joining());
        try (Reader r = new BufferedReader(new StringReader(ret))) {
            JSONReader reader = JSONFactory.instance().makeReader(r);
            return GitHubClient.parseTags(reader.build());
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public static Manifest getManifest(String user, String project, String version) {
        String url = String.format("https://raw.githubusercontent.com/%s/%s/%s/%s", user, project, version, Manifest.NAME);
        SysCommand curl = SysCommand.builder("curl")
                .addParameter("-s")
                .addParameter(url)
                .build();
        String ret = Executor.executeAndReadLines(curl).stream().collect(Collectors.joining("\n"));
        try (Reader r = new BufferedReader(new StringReader(ret))) {
            return Manifest.readFrom(new URI(url), r);
        } catch (IOException | URISyntaxException e) {
            throw new IllegalStateException(e);
        }
    }

    public static void fetchZipball(String user, String project, String version, Path targetDir) {
        try {
            Files.createDirectories(targetDir);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        String url = String.format("https://github.com/%s/%s/tarball/%s/", user, project, version);
        SysCommand curl = SysCommand.builder("curl")
                .addParameter("-sSL")
                .addParameter(url)
                .pipeTo("tar")
                .addParameter("-xz")
                .addParameter("--strip")
                .addParameter("1")
                .build();

        Executor.execute(null, targetDir, Collections.singletonList(curl));
    }

    public static void main(String[] args) throws IOException {
//        System.out.println(getTags("kppk", "somelibrary"));
//        System.out.println(getManifest("kppk", "somelibrary", "1.0.1"));
        Files.createDirectory(Paths.get("/tmp/my-lib"));
        fetchZipball("kppk", "somelibrary", "1.0.1", Paths.get("/tmp/my-lib"));
    }


}
