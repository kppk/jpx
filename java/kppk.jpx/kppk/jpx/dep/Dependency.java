package kppk.jpx.dep;

import kppk.jpx.util.Types;
import kppk.jpx.version.Version;

import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Resolved dependency value class.
 */
public final class Dependency {

    public final String name;
    public final Version version;
    public final List<Dependency> dependencies;
    public final Resolver resolver;

    public Dependency(String name, Version version, List<Dependency> dependencies, Resolver resolver) {
        this.name = name;
        this.version = version;
        this.dependencies = dependencies;
        this.resolver = resolver;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Dependency{");
        sb.append("repo='").append(name).append('\'');
        sb.append(", version=").append(version);
        sb.append(", dependencies=").append(dependencies);
        sb.append('}');
        return sb.toString();
    }

    public static Dependency read(Map<String, Object> vals) {
        try {
            String name = Types.safeCast(vals.get("repo"), String.class);
            Version version = new Version(Types.safeCast(vals.get("version"), String.class));
            Object source = vals.get("source");
            Resolver resolver = null;
            if (source != null) {
                String sourceString = Types.safeCast(source, String.class);
                resolver = Resolver.thatResolves(sourceString);
            }
            return new Dependency(name, version, null, resolver);
        } catch (ParseException e) {
            throw new IllegalStateException(e);
        }
    }

    public Map<String, ?> write() {
        Map<String, String> vals = new HashMap<>();
        vals.put("repo", name);
        vals.put("version", version.toString());
        if (resolver != null) {
            vals.put("source", resolver.toString());
        }
        return vals;
    }
}
