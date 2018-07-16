package kppk.jpx.model;

import kppk.jpx.util.Types;
import kppk.jpx.version.VersionRange;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * TODO: Document this
 */

public final class Dep {

    public final Name name;
    public final VersionRange version;
    public final String selectorName;
    public final String selectorValue;

    private Dep(Name name, VersionRange version, String selectorName, String selectorValue) {
        this.name = name;
        this.version = version;
        this.selectorName = selectorName;
        this.selectorValue = selectorValue;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Dep{");
        sb.append("repo='").append(name).append('\'');
        sb.append(", version=").append(version);
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
                Objects.equals(selectorName, dep.selectorName) &&
                Objects.equals(selectorValue, dep.selectorValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, version, selectorName, selectorValue);
    }

    static Dep read(Map.Entry<String, Object> entry) {
        Object value = entry.getValue();
        Name name = Name.parseName(entry.getKey());
        if (value instanceof Map) {
            Map<String, Object> values = Types.castToMap(value);
            if (values.size() != 2) {
                throw new IllegalArgumentException("Unexpected dep map size, expected 2, got " + values.size());
            }
            Object ver = values.get("version");
            if (ver == null) {
                throw new IllegalArgumentException("Missing 'version' in '" + name + "' dependency");
            }
            VersionRange version = VersionRange.parse(Types.safeCast(ver, String.class));
            Map.Entry<String, Object> selector = values.entrySet().stream()
                    .filter(e -> !e.getKey().equals("version"))
                    .findFirst()
                    .get();
            return new Dep(name, version, selector.getKey(), Types.safeCast(selector.getValue(), String.class));
        } else {
            // simple version range, defaults to github
            VersionRange version = VersionRange.parse(Types.safeCast(entry.getValue(), String.class));
            return new Dep(name, version, "github", null);
        }
    }

    Map.Entry<String, Object> write() {
        return new HashMap.SimpleEntry<>(name.toString(), version.toString());
    }


    public static final class Name {
        public final String selector;
        public final String org;
        public final String repo;

        private Name(String selector, String org, String repo) {
            this.selector = selector;
            this.org = org;
            this.repo = repo;
        }

        public static Name parseName(String name) {
            Objects.requireNonNull(name);
            String[] splits = name.split(":");
            if (splits.length == 1) {
                Pack.Name packName = Pack.Name.parse(splits[0]);
                return new Name(null, packName.org, packName.repo);
            } else if (splits.length == 2) {
                Pack.Name packName = Pack.Name.parse(splits[1]);
                return new Name(splits[0], packName.org, packName.repo);
            } else {
                throw new IllegalArgumentException("Invalid dep repo '" + name + "', must contain 0 or 1 colon");
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Name name = (Name) o;
            return Objects.equals(selector, name.selector) &&
                    Objects.equals(org, name.org) &&
                    Objects.equals(repo, name.repo);
        }

        @Override
        public int hashCode() {
            return Objects.hash(selector, org, repo);
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder();
            if (selector != null) {
                sb.append(selector).append(':');
            }
            sb.append(org).append("/").append(repo);
            return sb.toString();
        }
    }
}
