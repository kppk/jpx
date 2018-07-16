package kppk.jpx.dep;

import kppk.jpx.model.Dep;
import kppk.jpx.model.Manifest;
import kppk.jpx.version.Version;

import java.nio.file.Path;
import java.text.ParseException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * TODO: Document this
 */
final class GitHubResolver implements Resolver {

    public static final String SELECTOR = "github";

    private final Dep.Name name;


    GitHubResolver(Dep dep) {
        name = dep.name;
    }

    @Override
    public List<Version> listVersions() {
        return GitHubCurlClient.getTags(name.org, name.repo).stream()
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
        return GitHubCurlClient.getManifest(name.org, name.repo, version.toString());
    }

    @Override
    public void fetch(Version version, Path targetDir) {
        GitHubCurlClient.fetch(name.org, name.repo, version.toString(), targetDir);
    }
}
