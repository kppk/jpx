package kppk.jpx.dep;

import kppk.jpx.module.ModuleDescriptor;
import kppk.jpx.module.ModuleDescriptorReader;
import kppk.jpx.project.JavaProject;
import kppk.jpx.version.Version;

import java.nio.file.Path;

/**
 * Test of {@link GitHubResolver}.
 */
 public final class GitHubResolverTest {

    public static void main(String[] args) {
        testGetModuleDescriptor();
    }

    private static void testGetModuleDescriptor() {
        GitHubResolver resolver = new GitHubResolver("kppk", "somelibrary");
        Path path = JavaProject.asModuleInfoPath("kppk", "somelibrary");
        String rawDescriptor = resolver.getRawFile(new Version(1, 0, 2), path);

        ModuleDescriptor descriptor = ModuleDescriptorReader.readFrom(rawDescriptor);

        assert "kppk.somelibrary".equals(descriptor.name());
        assert descriptor.requires().size() == 1;
    }
}
