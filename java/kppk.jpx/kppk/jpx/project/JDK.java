package kppk.jpx.project;

import java.util.Arrays;
import java.util.Objects;

/**
 * TODO: Document this
 */
enum JDK {
    v8("8"), v9("9"), v10("10"), v11("11");

    public final String release;

    JDK(String name) {
        this.release = name;
    }

    public static JDK releaseOf(String release) {
        Objects.requireNonNull(release);
        return Arrays.asList(values()).stream()
                .filter(r -> r.release.equals(release))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown java release: " + release));
    }

}
