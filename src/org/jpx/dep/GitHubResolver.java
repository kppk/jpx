package org.jpx.dep;

import org.jpx.model.Dep;
import org.jpx.model.Manifest;
import org.jpx.version.Version;

import java.nio.file.Path;
import java.text.ParseException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * TODO: Document this
 */
public final class GitHubResolver implements Resolver {

    private final String user;
    private final String project;


    GitHubResolver(Dep dep) {
        String name = dep.name;
        int idx = name.indexOf(".");
        if (idx == -1) {
            throw new IllegalArgumentException("Invalid name, missing '.'");
        }
        user = name.substring(0, idx);
        project = name.substring(idx + 1);
    }

    static boolean canResolve(Dep dep) {
        return true;
    }

    @Override
    public List<Version> listVersions() {
        return GitHubCurlResolver.getTags(user, project).stream()
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
        return GitHubCurlResolver.getManifest(user, project, version.toString());
    }

    @Override
    public void fetch(Version version, Path targetDir) {
        GitHubCurlResolver.fetchZipball(user, project, version.toString(), targetDir);
    }
}
