package kppk.jpx.jdk;

import java.util.Locale;

/**
 * TODO: Document this
 */
final class Os {

    static final Type TYPE = getType();

    public enum Type {
        windows, mac, linux, other
    }

    private static Type getType() {
        String os = System.getProperty("os.name", "generic").toLowerCase(Locale.ENGLISH);
        if ((os.contains("mac")) || (os.contains("darwin"))) {
            return Type.mac;
        } else if (os.contains("win")) {
            return Type.windows;
        } else if (os.contains("nux")) {
            return Type.linux;
        } else {
            return Type.other;
        }
    }


}
