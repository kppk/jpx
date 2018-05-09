package org.jpx.cli;

import java.util.Objects;

/**
 * TODO: Document this
 */
public final class StringFlag implements Flag<String> {

    private final String name;
    private final String shortName;
    private final String usage;
    private final String defaultValue;

    private StringFlag(String name, String shortName, String usage, String defaultValue) {
        this.name = name;
        this.shortName = shortName;
        this.usage = usage;
        this.defaultValue = defaultValue;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getShortName() {
        return shortName;
    }

    @Override
    public String getUsage() {
        return usage;
    }

    @Override
    public String getDefaultValue() {
        return this.defaultValue;
    }

    @Override
    public String convert(String val) {
        return val;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StringFlag that = (StringFlag) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    public static StringFlagBuilder builder() {
        return new StringFlagBuilder();
    }

    public static class StringFlagBuilder {
        private String name;
        private String shortName;
        private String value;
        private String usage;

        private StringFlagBuilder() {
        }

        public StringFlagBuilder setName(String name) {
            this.name = name;
            return this;
        }

        public StringFlagBuilder setUsage(String usage) {
            this.usage = usage;
            return this;
        }

        public StringFlagBuilder setShortName(String shortName) {
            this.shortName = shortName;
            return this;
        }

        public StringFlagBuilder setDefaultValue(String value) {
            this.value = value;
            return this;
        }

        public StringFlag build() {
            return new StringFlag(name, shortName, usage, value);
        }
    }


}
