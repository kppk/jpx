package org.jpx.dep;

import org.jpx.model.Manifest;
import org.jpx.version.Version;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * TODO: Document this
 */
public final class Graph {

    public final Dependency root;

    private Graph(Dependency root) {
        this.root = root;
    }

    public static Graph from(Manifest manifest) {
        return new Graph(doResolve(manifest, null));
    }

    static Dependency doResolve(Manifest mf, Resolver resolvedBy) {
        List<Dependency> dependencies = mf.deps.stream()
                .map(dep -> {
                    Resolver r = Resolver.thatResolves(mf, dep);
                    Version v = r.latest(dep);
                    Manifest m = r.getManifest(v);
                    return doResolve(m, r);
                })
                .collect(Collectors.toList());
        return new Dependency(mf.pack.name, mf.pack.version, dependencies, resolvedBy);
    }

    public void printTree() {
        doPrintTree(root, 0);
    }


    private void doPrintTree(Dependency dependency, int indent) {
        for (int i = 0; i < indent; i++) {
            System.out.print("\t");
        }
        System.out.println("- " + dependency.name + ":" + dependency.version + " <-- " + dependency.resolver);
        dependency.dependencies.stream()
                .forEach(d -> doPrintTree(d, indent + 1));
    }


    public List<Dependency> flatten() {
        return doFlatten(root)
                .collect(Collectors.toList());
    }

    private Stream<Dependency> doFlatten(Dependency dep) {
        return Stream.concat(Stream.of(dep),
                dep.dependencies.stream().flatMap(this::doFlatten));
    }


}
