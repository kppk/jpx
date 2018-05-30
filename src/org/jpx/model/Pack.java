package org.jpx.model;

import org.jpx.util.Types;
import org.jpx.version.Version;

import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * TODO: Document this
 */
public final class Pack {

    public enum Type {
        LIBRARY("lib"),
        BINARY("bin");


        public final String name;

        Type(String name) {
            this.name = name;
        }

        static Type ofName(String name) {
            for (Type type : Type.values()) {
                if (type.name.equals(name)) {
                    return type;
                }
            }
            return null;
        }

    }

    public final String name;
    public final Version version;
    public final List<String> authors;
    public final Type type;
    public final String javaRelease;

    private Pack(String name, Version version, List<String> authors, Type type, String javaRelease) {
        this.name = name;
        this.version = version;
        this.authors = authors;
        this.type = type;
        this.javaRelease = javaRelease;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Pack{");
        sb.append("name='").append(name).append('\'');
        sb.append(", version=").append(version);
        sb.append(", authors=").append(authors);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pack pack = (Pack) o;
        return Objects.equals(name, pack.name) &&
                Objects.equals(version, pack.version) &&
                Objects.equals(authors, pack.authors) &&
                type == pack.type &&
                Objects.equals(javaRelease, pack.javaRelease);
    }

    @Override
    public int hashCode() {

        return Objects.hash(name, version, authors, type, javaRelease);
    }

    static Pack read(Map<String, Object> map) {
        String name = null;
        Version version = null;
        List<String> authors = null;
        Type type = null;
        String javaRelease = null;
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            switch (entry.getKey()) {
                case "name":
                    name = Types.safeCast(entry.getValue(), String.class);
                    break;
                case "version":
                    String versionString = Types.safeCast(entry.getValue(), String.class);
                    try {
                        version = new Version(versionString);
                    } catch (ParseException e) {
                        throw new ManifestException(e);
                    }
                    break;
                case "authors":
                    authors = Types.safeCast(entry.getValue(), List.class);
                    break;
                case "java_release":
                    javaRelease = Types.safeCast(entry.getValue(), String.class);
                    break;
                case "type":
                    String typeString = Types.safeCast(entry.getValue(), String.class);
                    type = Type.ofName(typeString);
                    if (type == null) {
                        throw new ManifestException("Invalid 'type' value");
                    }
                    break;

            }
        }
        return new Pack(name, version, authors, type, javaRelease);
    }

    Map<String, Object> write() {
        Map<String, Object> vals = new HashMap<>();
        vals.put("name", name);
        vals.put("version", version.toString());
        vals.put("authors", authors);
        vals.put("java_release", javaRelease);
        vals.put("type", type.name);
        return vals;
    }

}
