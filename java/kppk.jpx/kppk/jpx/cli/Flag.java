package kppk.jpx.cli;

/**
 * TODO: Document this
 */
public interface Flag<T> {

    String HYPHEN = "-";
    String DOUBLE_HYPHEN = "--";


    String getName();

    String getShortName();

    String getUsage();

    T getDefaultValue();

    T convert(String val);

    default boolean matches(String name) {
        return ("--" + getName()).equals(name) ||
                ("-" + getShortName()).equals(name);
    }

    static boolean isFlag(String name) {
        if (name != null) {
            return name.startsWith(HYPHEN) || name.startsWith(DOUBLE_HYPHEN);
        }
        return false;
    }


}
