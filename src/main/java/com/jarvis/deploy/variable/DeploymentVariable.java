package com.jarvis.deploy.variable;

import java.time.Instant;
import java.util.Objects;

/**
 * Represents a single deployment variable with scope, value, and metadata.
 */
public class DeploymentVariable {

    private final String key;
    private String value;
    private final VariableScope scope;
    private final String environment;
    private final Instant createdAt;
    private Instant updatedAt;
    private boolean sensitive;

    public DeploymentVariable(String key, String value, VariableScope scope, String environment) {
        Objects.requireNonNull(key, "key must not be null");
        Objects.requireNonNull(scope, "scope must not be null");
        this.key = key;
        this.value = value;
        this.scope = scope;
        this.environment = environment;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
        this.sensitive = false;
    }

    public String getKey() { return key; }

    public String getValue() { return value; }

    public void setValue(String value) {
        this.value = value;
        this.updatedAt = Instant.now();
    }

    public VariableScope getScope() { return scope; }

    public String getEnvironment() { return environment; }

    public Instant getCreatedAt() { return createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }

    public boolean isSensitive() { return sensitive; }

    public void setSensitive(boolean sensitive) { this.sensitive = sensitive; }

    public String getDisplayValue() {
        return sensitive ? "****" : value;
    }

    @Override
    public String toString() {
        return "DeploymentVariable{key='" + key + "', scope=" + scope +
               ", environment='" + environment + "', sensitive=" + sensitive + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DeploymentVariable)) return false;
        DeploymentVariable that = (DeploymentVariable) o;
        return Objects.equals(key, that.key) &&
               Objects.equals(environment, that.environment) &&
               scope == that.scope;
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, environment, scope);
    }
}
