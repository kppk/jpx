package org.jpx.dep;

import org.jpx.model.Dep;
import org.jpx.model.Manifest;

import java.util.List;
import java.util.stream.Collectors;

/**
 * TODO: Document this
 */
public interface Resolver {

    Manifest resolveManifest(Manifest parent, Dep dep);

    static Dependency doResolve(Manifest mf) {
        List<Dependency> dependencies = mf.deps.stream()
                .map(dep -> resolver(dep).resolveManifest(mf, dep))
                .map(m -> doResolve(m))
                .collect(Collectors.toList());
        return new Dependency(mf.pack.name, mf.pack.version, dependencies);
    }


    static Resolver resolver(Dep dep) {
        if (dep.path != null) {
            return new PathResolver();
        }
        throw new IllegalArgumentException("Don't know which resolver to use");
    }
}
