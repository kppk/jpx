package org.jpx.model;

import org.jpx.util.Types;
import org.jpx.version.Version;

import java.text.ParseException;
import java.util.List;
import java.util.Map;

/**
 * TODO: Document this
 */
public final class Pack {

    public final String name;
    public final Version version;
    public final List<String> authors;

    private Pack(String name, Version version, List<String> authors) {
        this.name = name;
        this.version = version;
        this.authors = authors;
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

    static Pack parse(Map<String, Object> map) {
        String name = null;
        Version version = null;
        List<String> authors = null;
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
            }
        }
        return new Pack(name, version, authors);
    }

}
