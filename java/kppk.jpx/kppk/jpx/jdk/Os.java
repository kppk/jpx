package kppk.jpx.jdk;

import java.util.Locale;

/**
 * Current Operating System info.
 */
final class Os {

    // note: this is going to be inlined by SubstrateVM, which is ok
    static final Type TYPE = getType();
    static final Arch ARCH = getArch();

    public enum Type {
        windows, mac, linux, other
    }

    public enum Arch {
        x64, other
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

    private static Arch getArch() {
        String arch = System.getProperty("os.arch");
        if ("x86_64".equals(arch)) {
            return Arch.x64;
        } else {
            return Arch.other;
        }
    }

    private Os() {
    }

}
