package kppk.jpx.dep;

import kppk.jpx.model.Dep;
import kppk.jpx.model.Manifest;
import kppk.jpx.module.ModuleDescriptor;
import kppk.jpx.version.Version;

import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;

/**
 * TODO: Document this
 */
public interface Resolver {

    List<Version> listVersions();

    Manifest getManifest(Version version);

    String getRawFile(Version version, Path path);

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
            return new FileCache(dep, new GitHubResolver(dep.name.org, dep.name.repo));
        }

        throw new IllegalArgumentException("Don't know which resolver to use");
    }

    static Resolver thatResolves(String string) {


        throw new IllegalArgumentException("Don't know which resolver to use");
    }
}
