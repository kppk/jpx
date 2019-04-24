package kppk.jpx.dep;

import kppk.jpx.model.Manifest;

import java.nio.file.Paths;

/**
 * Test of {@link Graph}.
 */
public class GraphTest {

    public static void main(String[] args) {

    }

    public static void testFrom() {
        Manifest manifest = Manifest.readFrom(Paths.get("./data/kppk/prj1/"));
    }

}
