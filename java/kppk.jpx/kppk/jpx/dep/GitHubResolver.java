package kppk.jpx.dep;

import kppk.jpx.model.Manifest;
import kppk.jpx.version.Version;

import java.nio.file.Path;
import java.text.ParseException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Gets source from GitHub.
 */
final class GitHubResolver implements Resolver {

    public static final String SELECTOR = "github";

    private final String org;
    private final String repo;


    GitHubResolver(String org, String repo) {
        this.org = org;
        this.repo = repo;
    }

    @Override
    public List<Version> listVersions() {
        return GitHubCurlClient.getTags(org, repo).stream()
                .map(s -> {
                    try {
                        return new Version(s);
                    } catch (ParseException e) {
                        // ignore
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public Manifest getManifest(Version version) {
        return GitHubCurlClient.getManifest(org, repo, version.toString());
    }

    @Override
    public String getRawFile(Version version, Path path) {
        return GitHubCurlClient.getProjecFileRaw(org, repo, version.toString(), path);
    }

    @Override
    public void fetch(Version version, Path targetDir) {
        GitHubCurlClient.fetch(org, repo, version.toString(), targetDir);
    }
}
