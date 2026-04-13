package com.jarvis.deploy.plugin;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Carries contextual information passed to each plugin during execution.
 */
public class PluginContext {

    private final String environment;
    private final String artifactVersion;
    private final String applicationName;
    private final Map<String, String> metadata;

    public PluginContext(String environment, String artifactVersion, String applicationName, Map<String, String> metadata) {
        this.environment = Objects.requireNonNull(environment, "environment must not be null");
        this.artifactVersion = Objects.requireNonNull(artifactVersion, "artifactVersion must not be null");
        this.applicationName = Objects.requireNonNull(applicationName, "applicationName must not be null");
        this.metadata = metadata != null ? Collections.unmodifiableMap(new HashMap<>(metadata)) : Collections.emptyMap();
    }

    public String getEnvironment() {
        return environment;
    }

    public String getArtifactVersion() {
        return artifactVersion;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public String getMetadataValue(String key) {
        return metadata.get(key);
    }

    @Override
    public String toString() {
        return "PluginContext{" +
                "environment='" + environment + '\'' +
                ", artifactVersion='" + artifactVersion + '\'' +
                ", applicationName='" + applicationName + '\'' +
                ", metadata=" + metadata +
                '}';
    }
}
