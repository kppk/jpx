package kppk.jpx.model;

import kppk.jpx.dep.Dependency;
import kppk.jpx.dep.Graph;
import kppk.jpx.project.JavaProject;

import java.nio.file.Paths;
import java.util.List;

/**
 * TODO: Document this
 */
public class ManifestTest {

    public static void main(String[] args) {
        //new ManifestTest().testParse();
    }

    public void testParse() {
        Manifest manifest = Manifest.readFrom(Paths.get("./data/kppk/prj1/"));


        Graph graph = Graph.from(JavaProject.createNew(manifest));
        List<Dependency> deps = graph.flatten();
        graph.printTree();
    }
}
