package org.jpx.cli;

import java.util.Objects;

/**
 * TODO: Document this
 */
public final class BooleanFlag implements Flag<Boolean> {

    private final String name;
    private final String shortName;
    private final String usage;
    private final Boolean defaultValue;

    private BooleanFlag(String name, String shortName, String usage, Boolean defaultValue) {
        this.name = name;
        this.shortName = shortName;
        this.usage = usage;
        this.defaultValue = defaultValue;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public String getShortName() {
        return null;
    }

    @Override
    public String getUsage() {
        return null;
    }

    @Override
    public Boolean getDefaultValue() {
        return false;
    }

    @Override
    public Boolean convert(String val) {
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BooleanFlag that = (BooleanFlag) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    public static BooleanFlagBuilder builder() {
        return new BooleanFlagBuilder();
    }

    public static class BooleanFlagBuilder {
        private String name;
        private String shortName;
        private Boolean value;
        private String usage;

        private BooleanFlagBuilder() {
        }

        public BooleanFlagBuilder setName(String name) {
            this.name = name;
            return this;
        }

        public BooleanFlagBuilder setUsage(String usage) {
            this.usage = usage;
            return this;
        }

        public BooleanFlagBuilder setShortName(String shortName) {
            this.shortName = shortName;
            return this;
        }

        public BooleanFlagBuilder setDefaultValue(Boolean value) {
            this.value = value;
            return this;
        }

        public BooleanFlag build() {
            return new BooleanFlag(name, shortName, usage, value);
        }
    }
}
