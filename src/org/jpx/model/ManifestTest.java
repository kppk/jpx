package org.jpx.model;

import org.jpx.dep.Dependency;
import org.jpx.dep.Resolver;

import java.nio.file.Paths;

/**
 * TODO: Document this
 */
public class ManifestTest {

    public static void main(String[] args) {
        new ManifestTest().testParse();
    }

    public void testParse() {
        Manifest manifest = Manifest.readFrom(Paths.get("./data/prj1/jpx.toml"));
        System.out.println(manifest);
    }
}
