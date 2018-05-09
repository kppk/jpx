package org.jpx.model;

import org.jpx.util.Types;
import org.jpx.version.Version;

import java.text.ParseException;
import java.util.Map;

/**
 * TODO: Document this
 */
public final class Dep {

    public final String name;
    public final Version version;
    public final String path;

    public Dep(String name, Version version, String path) {
        this.name = name;
        this.version = version;
        this.path = path;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Dep{");
        sb.append("name='").append(name).append('\'');
        sb.append(", version=").append(version);
        sb.append(", path='").append(path).append('\'');
        sb.append('}');
        return sb.toString();
    }


    static Dep parse(Map.Entry<String, Object> entry) {
        try {
            Object value = entry.getValue();
            String name = entry.getKey();
            String path = null;
            Version version = null;
            if (value instanceof Map) {
                Map<String, Object> valueMap = Types.castToMap(value, String.class, Object.class);
                for (Map.Entry<String, Object> verEntry : valueMap.entrySet()) {
                    switch (verEntry.getKey()) {
                        case "path":
                            path = Types.safeCast(verEntry.getValue(), String.class);
                            break;
                    }
                }
            } else {
                version = new Version(Types.safeCast(entry.getValue(), String.class));
            }
            return new Dep(name, version, path);
        } catch (ParseException e) {
            throw new ManifestException(e);
        }
    }
}
