package org.jpx.dep;

import org.jpx.model.Dep;
import org.jpx.model.Manifest;

/**
 * TODO: Document this
 */
public interface Resolver {

    Manifest resolve(Dep dep);

    static Resolver thatResolves(Manifest mf, Dep dep) {
        if (PathResolver.canResolve(dep)) {
            return new PathResolver(mf);
        }

        throw new IllegalArgumentException("Don't know which resolver to use");
    }
}
