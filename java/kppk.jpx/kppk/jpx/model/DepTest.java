package kppk.jpx.model;

import kppk.jpx.version.Version;

import java.text.ParseException;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;

/**
 * Unit test of {@link Dep}.
 */
public final class DepTest {

    private void testSimple() throws ParseException {
        Map.Entry<String, Object> e = newEntry("test/lib", "1.0.0 <= v < 2.0.0");
        Dep dep = Dep.read(e);
        assert dep.version.lowerBound.equals(new Version("1.0.0"));
        assert dep.version.upperBound.equals(new Version("2.0.0"));
        assert dep.name.toString().equals("test/lib");
        assert dep.selectorName.equals("github");
    }

    private void testSimpleWithSelector() throws ParseException {
        Map.Entry<String, Object> e = newEntry("github:test/lib", "1.0.0 <= v < 2.0.0");
        Dep dep = Dep.read(e);
        assert dep.version.lowerBound.equals(new Version("1.0.0"));
        assert dep.version.upperBound.equals(new Version("2.0.0"));
        assert dep.name.toString().equals("github:test/lib");
        assert dep.selectorName.equals("github");
    }

    private void testGitHub() throws ParseException {
        HashMap<String, String> vals = new HashMap<>();
        vals.put("github", "https://github.com/test/lib");
        vals.put("version", "1.0.0 <= v < 2.0.0");
        Map.Entry<String, Object> e = newEntry("test/lib", vals);
        Dep dep = Dep.read(e);
        assert dep.version.lowerBound.equals(new Version("1.0.0"));
        assert dep.version.upperBound.equals(new Version("2.0.0"));
        assert dep.name.toString().equals("test/lib");
        assert dep.selectorName.equals("github");
        assert dep.selectorValue.equals("https://github.com/test/lib");
    }

    public static void main(String[] args) throws Exception {
        DepTest depTest = new DepTest();
        depTest.testSimple();
        depTest.testGitHub();
        depTest.testSimpleWithSelector();
    }

    private static Map.Entry<String, Object> newEntry(String key, Object val) {
        return new AbstractMap.SimpleImmutableEntry<>(key, val);
    }
}
