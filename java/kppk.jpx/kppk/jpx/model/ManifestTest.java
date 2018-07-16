package kppk.jpx.model;

import kppk.jpx.dep.Graph;

import java.nio.file.Paths;

/**
 * TODO: Document this
 */
public class ManifestTest {

    public static void main(String[] args) {
        new ManifestTest().testParse();
    }

    public void testParse() {
        Manifest manifest = Manifest.readFrom(Paths.get("./data/prj1/"));


//        System.out.println(manifest);
        Graph graph = Graph.from(manifest);
        Lock lock = new Lock(graph.flatten(), new Lock.Meta("someValue"));
        lock.writeTo(Paths.get("./data/prj1"));

        Lock anotherLock = Lock.readFrom(Paths.get("./data/prj1"));
        System.out.println(anotherLock);

//        graph.printTree();
//        System.out.println("flatten:");
//        graph.flatten()
//                .forEach(dependency -> System.out.println("- " + dependency.repo + ":" + dependency.version + " <-- " + dependency.resolver));
//
//        System.out.println("HASH: " + Objects.hash(manifest.deps));
    }
}
