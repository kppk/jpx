package kppk.jpx.jdk;

/**
 * TODO: Document this
 */
public final class JdkRelease {

    public static void validate(String release) {
        try {
            int num = Integer.parseInt(release);
            if (num < 9) {
                throw new IllegalArgumentException("Invalid Java release number, supported values: 9,10,11,...");
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid Java release number, supported values: 9,10,11,...");
        }
    }

    private JdkRelease() {
    }
}
