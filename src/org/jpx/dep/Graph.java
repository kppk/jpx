package org.jpx.dep;

import org.jpx.model.Manifest;

import java.util.List;
import java.util.stream.Collectors;

/**
 * TODO: Document this
 */
public final class Graph {

    public final Dependency root;

    private Graph(Dependency root) {
        this.root = root;
    }

    public static Graph from(Manifest manifest) {
        return new Graph(doResolve(manifest));
    }

    static Dependency doResolve(Manifest mf) {
        List<Dependency> dependencies = mf.deps.stream()
                .map(dep -> Resolver.thatResolves(dep).resolve(mf, dep))
                .map(m -> doResolve(m))
                .collect(Collectors.toList());
        return new Dependency(mf.pack.name, mf.pack.version, dependencies);
    }


}
