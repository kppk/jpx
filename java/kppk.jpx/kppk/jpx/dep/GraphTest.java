package kppk.jpx.dep;

import kppk.jpx.model.Manifest;
import kppk.jpx.module.ModuleDescriptor;
import kppk.jpx.module.ModuleDescriptorReader;
import kppk.jpx.project.JavaProject;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Test of {@link Graph}.
 */
public final class GraphTest {

    public static void main(String[] args) {
        testFrom();
    }

    public static void testFrom() {
        Manifest manifest = Manifest.readFrom(Paths.get("./data/kppk/prj1/"));

        System.out.println(manifest.basedir);
        Path moduleInfo = JavaProject.asModuleInfoPath(manifest.pack.name);
        Path moduleFullPath = Paths.get(manifest.basedir).resolve(moduleInfo);

        ModuleDescriptor descriptor = ModuleDescriptorReader.readFrom(moduleFullPath);
        System.out.println(descriptor);
    }

}
