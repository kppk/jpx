package org.jpx.dep;

import org.jpx.model.Dep;
import org.jpx.model.Manifest;

import java.nio.file.Path;

/**
 * TODO: Document this
 */
public final class GitResolver implements Resolver {

    private static final String KEY_GIT = "git";

    @Override
    public Manifest resolve() {
        return null;
    }

    @Override
    public String fetch(Path dir) {
        return null;
    }

    public static boolean canResolve(Dep dep) {
        return dep.values.containsKey(KEY_GIT);
    }
}
