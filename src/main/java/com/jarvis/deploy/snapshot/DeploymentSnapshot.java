package com.jarvis.deploy.snapshot;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents a point-in-time snapshot of a deployment state,
 * used for capturing configuration and metadata before a deployment
 * so it can be restored during rollback.
 */
public class DeploymentSnapshot {

    private final String snapshotId;
    private final String environment;
    private final String artifactVersion;
    private final Instant capturedAt;
    private final Map<String, String> configProperties;
    private final String deployedBy;

    public DeploymentSnapshot(String environment, String artifactVersion,
                               Map<String, String> configProperties, String deployedBy) {
        this.snapshotId = UUID.randomUUID().toString();
        this.environment = Objects.requireNonNull(environment, "environment must not be null");
        this.artifactVersion = Objects.requireNonNull(artifactVersion, "artifactVersion must not be null");
        this.capturedAt = Instant.now();
        this.configProperties = Collections.unmodifiableMap(new HashMap<>(configProperties));
        this.deployedBy = deployedBy != null ? deployedBy : "unknown";
    }

    public String getSnapshotId() { return snapshotId; }
    public String getEnvironment() { return environment; }
    public String getArtifactVersion() { return artifactVersion; }
    public Instant getCapturedAt() { return capturedAt; }
    public Map<String, String> getConfigProperties() { return configProperties; }
    public String getDeployedBy() { return deployedBy; }

    @Override
    public String toString() {
        return String.format("DeploymentSnapshot{id='%s', env='%s', version='%s', capturedAt=%s}",
                snapshotId, environment, artifactVersion, capturedAt);
    }
}
