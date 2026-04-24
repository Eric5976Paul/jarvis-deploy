package com.jarvis.deploy.replay;

import java.time.Instant;
import java.util.Objects;

/**
 * Represents a request to replay a past deployment, reusing its configuration
 * and artifact for a target environment.
 */
public class DeploymentReplay {

    private final String replayId;
    private final String sourceDeploymentId;
    private final String targetEnvironment;
    private final String requestedBy;
    private final Instant requestedAt;
    private final boolean dryRun;

    public DeploymentReplay(String replayId, String sourceDeploymentId,
                            String targetEnvironment, String requestedBy,
                            boolean dryRun) {
        this.replayId = Objects.requireNonNull(replayId, "replayId must not be null");
        this.sourceDeploymentId = Objects.requireNonNull(sourceDeploymentId, "sourceDeploymentId must not be null");
        this.targetEnvironment = Objects.requireNonNull(targetEnvironment, "targetEnvironment must not be null");
        this.requestedBy = Objects.requireNonNull(requestedBy, "requestedBy must not be null");
        this.requestedAt = Instant.now();
        this.dryRun = dryRun;
    }

    public String getReplayId() { return replayId; }
    public String getSourceDeploymentId() { return sourceDeploymentId; }
    public String getTargetEnvironment() { return targetEnvironment; }
    public String getRequestedBy() { return requestedBy; }
    public Instant getRequestedAt() { return requestedAt; }
    public boolean isDryRun() { return dryRun; }

    @Override
    public String toString() {
        return String.format("DeploymentReplay{replayId='%s', source='%s', env='%s', dryRun=%s}",
                replayId, sourceDeploymentId, targetEnvironment, dryRun);
    }
}
