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
    public Boolean getDefaultValue() {
        return false;
    }

    @Override
    public Boolean convert(String val) {
        return Boolean.parseBoolean(val);
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

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("BooleanFlag{");
        sb.append("name='").append(name).append('\'');
        sb.append(", shortName='").append(shortName).append('\'');
        sb.append(", usage='").append(usage).append('\'');
        sb.append(", defaultValue=").append(defaultValue);
        sb.append('}');
        return sb.toString();
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
