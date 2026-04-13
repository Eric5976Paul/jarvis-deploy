package com.jarvis.deploy.checkpoint;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Represents a named checkpoint captured during a deployment pipeline,
 * storing the stage name, timestamp, status, and arbitrary metadata.
 */
public class DeploymentCheckpoint {

    private final String deploymentId;
    private final String stage;
    private final CheckpointStatus status;
    private final Instant capturedAt;
    private final Map<String, String> metadata;

    public DeploymentCheckpoint(String deploymentId, String stage,
                                CheckpointStatus status, Map<String, String> metadata) {
        this.deploymentId = Objects.requireNonNull(deploymentId, "deploymentId must not be null");
        this.stage = Objects.requireNonNull(stage, "stage must not be null");
        this.status = Objects.requireNonNull(status, "status must not be null");
        this.capturedAt = Instant.now();
        this.metadata = metadata != null ? Collections.unmodifiableMap(new HashMap<>(metadata)) : Collections.emptyMap();
    }

    public String getDeploymentId() { return deploymentId; }
    public String getStage() { return stage; }
    public CheckpointStatus getStatus() { return status; }
    public Instant getCapturedAt() { return capturedAt; }
    public Map<String, String> getMetadata() { return metadata; }

    public boolean isPassed() {
        return status == CheckpointStatus.PASSED;
    }

    @Override
    public String toString() {
        return String.format("DeploymentCheckpoint{deploymentId='%s', stage='%s', status=%s, capturedAt=%s}",
                deploymentId, stage, status, capturedAt);
    }
}
