package com.jarvis.deploy.label;

import java.util.Objects;

public class DeploymentLabel {
    private final String key;
    private final String value;
    private final boolean propagatable;

    public DeploymentLabel(String key, String value, boolean propagatable) {
        if (key == null || key.isBlank()) throw new IllegalArgumentException("Label key must not be blank");
        this.key = key;
        this.value = value != null ? value : "";
        this.propagatable = propagatable;
    }

    public DeploymentLabel(String key, String value) {
        this(key, value, false);
    }

    public String getKey() { return key; }
    public String getValue() { return value; }
    public boolean isPropagatable() { return propagatable; }

    public DeploymentLabel withValue(String newValue) {
        return new DeploymentLabel(this.key, newValue, this.propagatable);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DeploymentLabel)) return false;
        DeploymentLabel that = (DeploymentLabel) o;
        return Objects.equals(key, that.key) && Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() { return Objects.hash(key, value); }

    @Override
    public String toString() { return key + "=" + value; }
}
