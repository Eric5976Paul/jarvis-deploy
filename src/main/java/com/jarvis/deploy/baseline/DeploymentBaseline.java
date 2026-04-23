package com.jarvis.deploy.baseline;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Represents a captured baseline state of a deployment environment,
 * used for drift detection and compliance checks.
 */
public class DeploymentBaseline {

    private final String baselineId;
    private final String environment;
    private final String version;
    private final Instant capturedAt;
    private final Map<String, String> properties;
    private final String capturedBy;

    public DeploymentBaseline(String baselineId, String environment, String version,
                               Instant capturedAt, Map<String, String> properties, String capturedBy) {
        this.baselineId = Objects.requireNonNull(baselineId, "baselineId must not be null");
        this.environment = Objects.requireNonNull(environment, "environment must not be null");
        this.version = Objects.requireNonNull(version, "version must not be null");
        this.capturedAt = Objects.requireNonNull(capturedAt, "capturedAt must not be null");
        this.properties = Collections.unmodifiableMap(new HashMap<>(Objects.requireNonNull(properties)));
        this.capturedBy = capturedBy;
    }

    public String getBaselineId() { return baselineId; }
    public String getEnvironment() { return environment; }
    public String getVersion() { return version; }
    public Instant getCapturedAt() { return capturedAt; }
    public Map<String, String> getProperties() { return properties; }
    public String getCapturedBy() { return capturedBy; }

    public boolean hasProperty(String key) {
        return properties.containsKey(key);
    }

    public String getProperty(String key) {
        return properties.get(key);
    }

    @Override
    public String toString() {
        return "DeploymentBaseline{baselineId='" + baselineId + "', environment='" + environment +
               "', version='" + version + "', capturedAt=" + capturedAt + ", capturedBy='" + capturedBy + "'}";
    }
}
