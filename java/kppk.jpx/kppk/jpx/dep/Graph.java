package kppk.jpx.dep;

import kppk.jpx.model.Manifest;
import kppk.jpx.module.ModuleDescriptor;
import kppk.jpx.module.ModuleDescriptorReader;
import kppk.jpx.project.JavaProject;
import kppk.jpx.version.Version;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Dependency graph.
 */
public final class Graph {

    public final Dependency root;

    private Graph(Dependency root) {
        this.root = root;
    }

    public static Graph from(JavaProject project) {

        return new Graph(doResolve(project.manifest, project.getModuleDescriptor(), null));
    }

    private static Dependency doResolve(Manifest mf, ModuleDescriptor modDesc, Resolver resolvedBy) {
        List<Dependency> dependencies = filterRequires(modDesc.requires().stream())
                .map(req -> {

                    Resolver r = getResolver(req);
                    Version v = r.latest();
                    Manifest m = r.getManifest(v);
                    ModuleDescriptor md = resolveModuleDescriptor(m, v, r);

                    return doResolve(m, md, r);
                })
                .collect(Collectors.toList());
        return new Dependency(mf.pack.name.toString(), mf.pack.version, dependencies, resolvedBy);
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

    private static ModuleDescriptor resolveModuleDescriptor(Manifest mf, Version version, Resolver resolver) {
        Path path = JavaProject.asModuleInfoPath(mf.pack.name);
        String rawModuleDescriptor = resolver.getRawFile(version, path);
        return ModuleDescriptorReader.readFrom(rawModuleDescriptor);
    }

    private static Resolver getResolver(ModuleDescriptor.Requires req) {
        String name = req.name();
        String[] splits = name.split("\\.");
        if (splits.length != 2) {
            throw new IllegalArgumentException("Invalid dependency name, expected org.repo");
        }
        String org = splits[0];
        String repo = splits[1];

        return Resolver.thatResolves(org, repo);
    }

    private static Stream<ModuleDescriptor.Requires> filterRequires(Stream<ModuleDescriptor.Requires> requires) {
        return requires
                .filter(r -> !r.modifiers().contains(ModuleDescriptor.Requires.Modifier.MANDATED)
                        && !r.name().startsWith("java")
                        && !r.name().startsWith("jdk")
                );
    }

}
