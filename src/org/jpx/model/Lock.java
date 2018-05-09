package org.jpx.model;

import java.util.List;

/**
 * TODO: Document this
 */
public final class Lock {

    public final List<Lib> libs;
    public final Meta meta;

    public Lock(List<Lib> libs, Meta meta) {
        this.libs = libs;
        this.meta = meta;
    }

    public static final class Meta {
        public final String inputDigest;

        public Meta(String inputDigest) {
            this.inputDigest = inputDigest;
        }
    }

}
