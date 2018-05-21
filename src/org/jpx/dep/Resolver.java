package org.jpx.dep;

import org.jpx.model.Dep;
import org.jpx.model.Manifest;

import java.nio.file.Path;

/**
 * TODO: Document this
 */
public interface Resolver {

    Manifest resolve();

    String fetch(Path dir);

    static Resolver thatResolves(Manifest mf, Dep dep) {
        if (PathResolver.canResolve(dep)) {
            return new PathResolver(mf, dep);
        }
        if (GitResolver.canResolve(dep)) {
            return new GitResolver();
        }

        throw new IllegalArgumentException("Don't know which resolver to use");
    }

    static Resolver thatResolves(String string) {
        if (PathResolver.canResolve(string)) {
            return PathResolver.fromString(string);
        }

        throw new IllegalArgumentException("Don't know which resolver to use");
    }
}
