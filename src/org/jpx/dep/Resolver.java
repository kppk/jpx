package org.jpx.dep;

import org.jpx.cache.FileCache;
import org.jpx.model.Dep;
import org.jpx.model.Manifest;
import org.jpx.version.Version;

import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;

/**
 * TODO: Document this
 */
public interface Resolver {

    List<Version> listVersions();

    Manifest getManifest(Version version);

    void fetch(Version version, Path targetDir);

    default Version latest(Dep dep) {
        return listVersions().stream()
                .sorted(Comparator.reverseOrder())
                .filter(dep.version::accepts)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        String.format("No suitable version found for %s in range '%s'", dep.name, dep.version)
                ));
    }

    static Resolver thatResolves(Manifest mf, Dep dep) {

        if (dep.selectorName.equals(GitHubResolver.SELECTOR)) {
            return new FileCache(dep, new GitHubResolver(dep));
        }

        throw new IllegalArgumentException("Don't know which resolver to use");
    }

    static Resolver thatResolves(String string) {


        throw new IllegalArgumentException("Don't know which resolver to use");
    }
}
