package org.jpx.model;

import org.jpx.util.Types;
import org.jpx.version.VersionRange;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * TODO: Document this
 */
public final class Dep {

    public final String name;
    public final VersionRange version;
    public final Map<String, Object> values;

    public Dep(String name, VersionRange version, Map<String, Object> values) {
        this.name = name;
        this.version = version;
        this.values = values;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Dep{");
        sb.append("name='").append(name).append('\'');
        sb.append(", version=").append(version);
        sb.append(", values='").append(values).append('\'');
        sb.append('}');
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Dep dep = (Dep) o;
        return Objects.equals(name, dep.name) &&
                Objects.equals(version, dep.version) &&
                Objects.equals(values, dep.values);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, version, values);
    }

    static Dep read(Map.Entry<String, Object> entry) {
        Object value = entry.getValue();
        String name = entry.getKey();
        Map<String, Object> values = Collections.emptyMap();
        VersionRange version = null;
        if (value instanceof Map) {
            values = Types.castToMap(value);
        } else {
            version = VersionRange.parse(Types.safeCast(entry.getValue(), String.class));
        }
        return new Dep(name, version, values);
    }

    Map.Entry<String, Object> write() {
        return new HashMap.SimpleEntry<>(name, version.toString());
    }
}
