package org.jpx.dep;

import org.jpx.model.Dep;
import org.jpx.model.Manifest;
import org.jpx.util.Types;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;

/**
 * TODO: Document this
 */
public final class GitHubResolver implements Resolver {

    private static final String KEY_GIT = "git";

    private final String name;
    private final URL gitUrl;

    GitHubResolver(Dep dep) {
        String gitUrl = Types.safeCast(dep.values.get(KEY_GIT), String.class);
        if (gitUrl == null) {
            throw new IllegalArgumentException("Missing git");
        }
        try {
            this.gitUrl = new URL(gitUrl);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Invalid git url: " + gitUrl);
        }
        this.name = dep.name;
    }

    static boolean canResolve(Dep dep) {
        if (!dep.values.containsKey(KEY_GIT)) {
            return false;
        }

        String gitUrl = Types.safeCast(dep.values.get(KEY_GIT), String.class);
        if (gitUrl == null) {
            return false;
        }
        return gitUrl.startsWith("https://github.com/");
    }

    @Override
    public Manifest resolve() {
        
        return null;
    }

    @Override
    public String fetch(Path dir) {
        return null;
    }

}
