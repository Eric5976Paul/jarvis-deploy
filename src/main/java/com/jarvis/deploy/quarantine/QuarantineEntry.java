package com.jarvis.deploy.quarantine;

import java.time.Instant;
import java.util.Objects;

public class QuarantineEntry {

    private final String deploymentId;
    private final String environment;
    private final QuarantineReason reason;
    private final String details;
    private final Instant quarantinedAt;
    private boolean released;

    public QuarantineEntry(String deploymentId, String environment, QuarantineReason reason, String details) {
        this.deploymentId = Objects.requireNonNull(deploymentId, "deploymentId must not be null");
        this.environment = Objects.requireNonNull(environment, "environment must not be null");
        this.reason = Objects.requireNonNull(reason, "reason must not be null");
        this.details = details;
        this.quarantinedAt = Instant.now();
        this.released = false;
    }

    public String getDeploymentId() { return deploymentId; }
    public String getEnvironment() { return environment; }
    public QuarantineReason getReason() { return reason; }
    public String getDetails() { return details; }
    public Instant getQuarantinedAt() { return quarantinedAt; }
    public boolean isReleased() { return released; }

    public void release() {
        this.released = true;
    }

    @Override
    public String toString() {
        return "QuarantineEntry{deploymentId='" + deploymentId + "', environment='" + environment +
               "', reason=" + reason + ", released=" + released + ", quarantinedAt=" + quarantinedAt + "}";
    }
}
