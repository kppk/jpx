package kppk.jpx.model;

import kppk.jpx.util.Types;
import kppk.jpx.version.Version;

import java.text.ParseException;
import java.util.HashMap;
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

    public final Name name;
    public final Version version;
    public final Type type;
    public final String javaRelease;

    private Pack(Name name, Version version, Type type, String javaRelease) {
        this.name = name;
        this.version = version;
        this.type = type;
        this.javaRelease = javaRelease;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Pack{");
        sb.append("name='").append(name).append('\'');
        sb.append(", version=").append(version);
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
                type == pack.type &&
                Objects.equals(javaRelease, pack.javaRelease);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, version, type, javaRelease);
    }

    static Pack read(Map<String, Object> map) {
        Name name = null;
        Version version = null;
        Type type = null;
        String javaRelease = null;
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            switch (entry.getKey()) {
                case "name":
                    name = Name.parse(Types.safeCast(entry.getValue(), String.class));
                    break;
                case "version":
                    String versionString = Types.safeCast(entry.getValue(), String.class);
                    try {
                        version = new Version(versionString);
                    } catch (ParseException e) {
                        throw new ManifestException(e);
                    }
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
        return new Pack(name, version, type, javaRelease);
    }

    Map<String, Object> write() {
        Map<String, Object> vals = new HashMap<>();
        vals.put("name", name.toString());
        vals.put("version", version.toString());
        vals.put("java_release", javaRelease);
        vals.put("type", type.name);
        return vals;
    }

    public static final class Name {
        public final String org;
        public final String repo;

        private static final String NOT_ALFANUMERIC = "[^A-Za-z0-9]";

        private Name(String org, String repo) {
            this.org = org;
            this.repo = repo;
        }

        public static Name parse(String name) {
            Objects.requireNonNull(name);

            String[] splits = name.split("/");
            if (splits.length != 2) {
                throw new IllegalArgumentException("Invalid pack repo '" + name + "', repo must be in 'org/repo'");
            }
            String org = splits[0].replaceAll(NOT_ALFANUMERIC, "").trim();
            String repo = splits[1].replaceAll(NOT_ALFANUMERIC, "").trim();
            return new Name(org, repo);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Name name = (Name) o;
            return Objects.equals(org, name.org) &&
                    Objects.equals(repo, name.repo);
        }

        @Override
        public int hashCode() {

            return Objects.hash(org, repo);
        }

        @Override
        public String toString() {
            return org + "/" + repo;
        }
    }

}
