package org.jpx.version;

import java.text.ParseException;
import java.util.Objects;

/**
 * TODO: Document this
 */
public class VersionRange {
    public final Version lowerBound;
    public final Version upperBound;

    private VersionRange(Version lowerBound, Version upperBound) {
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
    }

    public boolean accepts(Version version) {
        Objects.requireNonNull(version);

        if (lowerBound.equals(version)) {
            return true;
        }
        if (lowerBound.compareTo(version) < 0 && upperBound.compareTo(version) > 0) {
            return true;
        }
        return false;
    }

    public static VersionRange parse(String range) {
        Objects.requireNonNull(range);

        String[] splits = range.split(" ");
        if (splits.length != 5) {
            throw new IllegalArgumentException("Expected range in a format: '1.0.0 <= v < 2.0.0'");
        }
        try {
            Version lower = new Version(splits[0]);
            if (!splits[1].equals("<=")) {
                throw new IllegalArgumentException("Expecting '<=', got " + splits[1]);
            }
            if (!splits[2].equals("v")) {
                throw new IllegalArgumentException("Expecting 'v', got " + splits[2]);
            }
            if (!splits[3].equals("<")) {
                throw new IllegalArgumentException("Expecting '<', got " + splits[3]);
            }
            Version upper = new Version(splits[4]);
            return new VersionRange(lower, upper);
        } catch (ParseException e) {
            throw new IllegalArgumentException("Invalid range");
        }
    }

    public String toString() {
        return lowerBound.toString() + " <= v < " + upperBound.toString();
    }

    public static void main(String[] args) throws ParseException {
        VersionRange range = VersionRange.parse("1.0.0 <= v < 2.0.0");
        System.out.println(range.lowerBound);
        System.out.println(range.upperBound);
        System.out.println(range.accepts(new Version("1.0.0")));
        System.out.println(range.accepts(new Version("1.5.1")));
        System.out.println(range.accepts(new Version("2.0.0")));
        System.out.println(range.accepts(new Version("3.0.0")));
    }

}
