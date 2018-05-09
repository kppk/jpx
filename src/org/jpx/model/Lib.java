package org.jpx.model;

import org.jpx.version.Version;

/**
 * TODO: Document this
 */
public final class Lib {
    public final String name;
    public final Version version;
    public final String checksum;

    public Lib(String name, Version version, String checksum) {
        this.name = name;
        this.version = version;
        this.checksum = checksum;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Lib{");
        sb.append("name='").append(name).append('\'');
        sb.append(", version=").append(version);
        sb.append(", checksum='").append(checksum).append('\'');
        sb.append('}');
        return sb.toString();
    }


}
