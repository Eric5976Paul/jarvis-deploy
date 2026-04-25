package com.jarvis.deploy.watchdog;

import java.time.Duration;
import java.time.Instant;

/**
 * Internal record representing a deployment being monitored by the watchdog.
 */
class WatchdogEntry {

    private final String deploymentId;
    private final String environment;
    private final Instant startedAt;
    private final Duration stallThreshold;

    WatchdogEntry(String deploymentId, String environment, Instant startedAt, Duration stallThreshold) {
        this.deploymentId = deploymentId;
        this.environment = environment;
        this.startedAt = startedAt;
        this.stallThreshold = stallThreshold;
    }

    String getDeploymentId() { return deploymentId; }
    String getEnvironment() { return environment; }
    Instant getStartedAt() { return startedAt; }
    Duration getStallThreshold() { return stallThreshold; }
}
