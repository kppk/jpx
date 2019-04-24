package kppk.jpx.module;

import java.io.StringReader;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Test of {@link ModuleDescriptorReader}.
 */
public class ModuleDescriptorReaderTest {

    public static void main(String[] args) throws Exception {
        testTest1();
        testTest2();
    }

    private static void testTest1() throws Exception {
        String test1 = "module com.example.foo {\n" +
                "    requires com.example.foo.http;\n" +
                "    requires java.logging;\n" +
                "\n" +
                "    requires transitive com.example.foo.network;\n" +
                "\n" +
                "    exports com.example.foo.bar;\n" +
                "    exports com.example.foo.internal to com.example.foo.probe;\n" +
                "\n" +
                "    opens com.example.foo.quux;\n" +
                "    opens com.example.foo.internal to com.example.foo.network,\n" +
                "                                      com.example.foo.probe;\n" +
                "\n" +
                "    uses com.example.foo.spi.Intf;\n" +
                "    provides com.example.foo.spi.Intf with com.example.foo.Impl;\n" +
                "}";
        StringReader reader = new StringReader(test1);
        ModuleDescriptorReader moduleReader = new ModuleDescriptorReader(reader);
        ModuleDescriptor descriptor = moduleReader.read();
        assert "com.example.foo".equals(descriptor.name());
        assert !descriptor.isOpen();

        List<ModuleDescriptor.Requires> reqs = descriptor.requires().stream()
                .filter(r -> !r.modifiers().contains(ModuleDescriptor.Requires.Modifier.MANDATED))
                .sorted()
                .collect(Collectors.toList());

        assert reqs.size() == 3;

        ModuleDescriptor.Requires req0 = reqs.get(0);
        assert "com.example.foo.http".equals(req0.name());
        assert req0.modifiers().size() == 0;

        ModuleDescriptor.Requires req1 = reqs.get(1);
        assert "com.example.foo.network".equals(req1.name());
        assert req1.modifiers().size() == 1;
        assert req1.modifiers().contains(ModuleDescriptor.Requires.Modifier.TRANSITIVE);

        ModuleDescriptor.Requires req2 = reqs.get(2);
        assert "java.logging".equals(req2.name());
        assert req2.modifiers().size() == 0;

    }

    private static void testTest2() throws Exception {
        String test2 = "open module com.example.foo {\n" +
                "    requires com.example.foo.http;\n" +
                "    requires java.logging;\n" +
                "\n" +
                "    requires transitive com.example.foo.network;\n" +
                " \n" +
                "    exports com.example.foo.bar;\n" +
                "    exports com.example.foo.internal to com.example.foo.probe;\n" +
                "\n" +
                "    uses com.example.foo.spi.Intf;\n" +
                "    provides com.example.foo.spi.Intf with com.example.foo.Impl;\n" +
                "}";
        StringReader reader = new StringReader(test2);
        ModuleDescriptorReader moduleReader = new ModuleDescriptorReader(reader);
        ModuleDescriptor descriptor = moduleReader.read();

        assert "com.example.foo".equals(descriptor.name());
        assert descriptor.isOpen();

        List<ModuleDescriptor.Requires> reqs = descriptor.requires().stream()
                .filter(r -> !r.modifiers().contains(ModuleDescriptor.Requires.Modifier.MANDATED))
                .sorted()
                .collect(Collectors.toList());

        assert reqs.size() == 3;

        ModuleDescriptor.Requires req0 = reqs.get(0);
        assert "com.example.foo.http".equals(req0.name());
        assert req0.modifiers().size() == 0;

        ModuleDescriptor.Requires req1 = reqs.get(1);
        assert "com.example.foo.network".equals(req1.name());
        assert req1.modifiers().size() == 1;
        assert req1.modifiers().contains(ModuleDescriptor.Requires.Modifier.TRANSITIVE);

        ModuleDescriptor.Requires req2 = reqs.get(2);
        assert "java.logging".equals(req2.name());
        assert req2.modifiers().size() == 0;
    }
}
