package com.jarvis.deploy.template;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Represents a reusable deployment template that captures common
 * configuration parameters for a given application or environment profile.
 */
public class DeploymentTemplate {

    private final String name;
    private final String environment;
    private final String artifactPattern;
    private final Map<String, String> defaultProperties;
    private final int maxRetries;
    private final boolean healthCheckEnabled;

    private DeploymentTemplate(Builder builder) {
        this.name = Objects.requireNonNull(builder.name, "Template name must not be null");
        this.environment = Objects.requireNonNull(builder.environment, "Environment must not be null");
        this.artifactPattern = builder.artifactPattern;
        this.defaultProperties = Collections.unmodifiableMap(new HashMap<>(builder.defaultProperties));
        this.maxRetries = builder.maxRetries;
        this.healthCheckEnabled = builder.healthCheckEnabled;
    }

    public String getName() { return name; }
    public String getEnvironment() { return environment; }
    public String getArtifactPattern() { return artifactPattern; }
    public Map<String, String> getDefaultProperties() { return defaultProperties; }
    public int getMaxRetries() { return maxRetries; }
    public boolean isHealthCheckEnabled() { return healthCheckEnabled; }

    public static Builder builder(String name, String environment) {
        return new Builder(name, environment);
    }

    public static class Builder {
        private final String name;
        private final String environment;
        private String artifactPattern = "*.jar";
        private final Map<String, String> defaultProperties = new HashMap<>();
        private int maxRetries = 3;
        private boolean healthCheckEnabled = true;

        private Builder(String name, String environment) {
            this.name = name;
            this.environment = environment;
        }

        public Builder artifactPattern(String artifactPattern) {
            this.artifactPattern = artifactPattern;
            return this;
        }

        public Builder property(String key, String value) {
            this.defaultProperties.put(key, value);
            return this;
        }

        public Builder maxRetries(int maxRetries) {
            this.maxRetries = maxRetries;
            return this;
        }

        public Builder healthCheckEnabled(boolean healthCheckEnabled) {
            this.healthCheckEnabled = healthCheckEnabled;
            return this;
        }

        public DeploymentTemplate build() {
            return new DeploymentTemplate(this);
        }
    }

    @Override
    public String toString() {
        return "DeploymentTemplate{name='" + name + "', environment='" + environment +
                "', maxRetries=" + maxRetries + ", healthCheckEnabled=" + healthCheckEnabled + "}";
    }
}
