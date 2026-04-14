package com.jarvis.deploy.timeout;

import java.time.Duration;
import java.time.Instant;

/**
 * Represents a deployment timeout configuration and tracks elapsed time.
 */
public class DeploymentTimeout {

    private final String deploymentId;
    private final Duration maxDuration;
    private final Instant startedAt;

    public DeploymentTimeout(String deploymentId, Duration maxDuration) {
        if (deploymentId == null || deploymentId.isBlank()) {
            throw new IllegalArgumentException("deploymentId must not be blank");
        }
        if (maxDuration == null || maxDuration.isNegative() || maxDuration.isZero()) {
            throw new IllegalArgumentException("maxDuration must be a positive duration");
        }
        this.deploymentId = deploymentId;
        this.maxDuration = maxDuration;
        this.startedAt = Instant.now();
    }

    public String getDeploymentId() {
        return deploymentId;
    }

    public Duration getMaxDuration() {
        return maxDuration;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public Duration getElapsed() {
        return Duration.between(startedAt, Instant.now());
    }

    public boolean isExceeded() {
        return getElapsed().compareTo(maxDuration) >= 0;
    }

    public Duration getRemaining() {
        Duration remaining = maxDuration.minus(getElapsed());
        return remaining.isNegative() ? Duration.ZERO : remaining;
    }

    @Override
    public String toString() {
        return "DeploymentTimeout{deploymentId='" + deploymentId +
               "', maxDuration=" + maxDuration +
               ", startedAt=" + startedAt + "}";
    }
}
