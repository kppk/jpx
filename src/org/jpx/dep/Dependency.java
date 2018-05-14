package org.jpx.dep;

import org.jpx.version.Version;

import java.util.List;

/**
 * TODO: Document this
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
        sb.append("name='").append(name).append('\'');
        sb.append(", version=").append(version);
        sb.append(", dependencies=").append(dependencies);
        sb.append('}');
        return sb.toString();
    }
}
