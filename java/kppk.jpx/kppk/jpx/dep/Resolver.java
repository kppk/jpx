package kppk.jpx.dep;

import kppk.jpx.model.Manifest;
import kppk.jpx.version.Version;

import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;

/**
 * Resolver is responsible for communication with source repository.
 */
public interface Resolver {

    List<Version> listVersions();

    Manifest getManifest(Version version);

    String getRawFile(Version version, Path path);

    void fetch(Version version, Path targetDir);

    default Version latest() {
        return listVersions().stream()
                .sorted(Comparator.reverseOrder())
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No suitable version found"));
    }

    static Resolver thatResolves(String org, String repo) {
        // use github resolver for everything now
        return new FileCache(org, repo, new GitHubResolver(org, repo));
    }

    static Resolver thatResolves(String string) {


        throw new IllegalArgumentException("Don't know which resolver to use");
    }
}
