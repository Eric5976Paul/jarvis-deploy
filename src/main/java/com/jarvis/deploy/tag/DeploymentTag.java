package com.jarvis.deploy.tag;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Represents a named tag attached to a deployment, carrying metadata
 * such as environment, version, and arbitrary key-value labels.
 */
public class DeploymentTag {

    private final String name;
    private final String environment;
    private final String version;
    private final Instant createdAt;
    private final Map<String, String> labels;

    public DeploymentTag(String name, String environment, String version, Map<String, String> labels) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Tag name must not be blank");
        }
        if (environment == null || environment.isBlank()) {
            throw new IllegalArgumentException("Environment must not be blank");
        }
        this.name = name;
        this.environment = environment;
        this.version = Objects.requireNonNull(version, "version must not be null");
        this.createdAt = Instant.now();
        this.labels = labels != null ? Collections.unmodifiableMap(new HashMap<>(labels)) : Collections.emptyMap();
    }

    public String getName() { return name; }
    public String getEnvironment() { return environment; }
    public String getVersion() { return version; }
    public Instant getCreatedAt() { return createdAt; }
    public Map<String, String> getLabels() { return labels; }

    public boolean hasLabel(String key) {
        return labels.containsKey(key);
    }

    public String getLabel(String key) {
        return labels.get(key);
    }

    @Override
    public String toString() {
        return String.format("DeploymentTag{name='%s', env='%s', version='%s', labels=%s}",
                name, environment, version, labels);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DeploymentTag)) return false;
        DeploymentTag that = (DeploymentTag) o;
        return Objects.equals(name, that.name) && Objects.equals(environment, that.environment);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, environment);
    }
}
