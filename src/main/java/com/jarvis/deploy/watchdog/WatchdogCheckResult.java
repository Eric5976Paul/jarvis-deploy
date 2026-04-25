package com.jarvis.deploy.watchdog;

import java.time.Duration;

/**
 * Represents the result of a watchdog health check for a monitored deployment.
 */
public class WatchdogCheckResult {

    public enum Status { HEALTHY, STALLED, NOT_WATCHED }

    private final String deploymentId;
    private final String environment;
    private final Status status;
    private final Duration elapsed;
    private final String message;

    private WatchdogCheckResult(String deploymentId, String environment, Status status, Duration elapsed, String message) {
        this.deploymentId = deploymentId;
        this.environment = environment;
        this.status = status;
        this.elapsed = elapsed;
        this.message = message;
    }

    public static WatchdogCheckResult healthy(String deploymentId, String environment, Duration elapsed) {
        return new WatchdogCheckResult(deploymentId, environment, Status.HEALTHY, elapsed,
                "Deployment is progressing normally.");
    }

    public static WatchdogCheckResult stalled(String deploymentId, String environment, Duration elapsed) {
        return new WatchdogCheckResult(deploymentId, environment, Status.STALLED, elapsed,
                "Deployment has exceeded its stall threshold and may be hung.");
    }

    public static WatchdogCheckResult notWatched(String deploymentId) {
        return new WatchdogCheckResult(deploymentId, null, Status.NOT_WATCHED, null,
                "Deployment is not currently being watched.");
    }

    public String getDeploymentId() { return deploymentId; }
    public String getEnvironment() { return environment; }
    public Status getStatus() { return status; }
    public Duration getElapsed() { return elapsed; }
    public String getMessage() { return message; }
    public boolean isStalled() { return status == Status.STALLED; }
    public boolean isHealthy() { return status == Status.HEALTHY; }

    @Override
    public String toString() {
        return String.format("WatchdogCheckResult{id='%s', env='%s', status=%s, elapsed=%s}",
                deploymentId, environment, status, elapsed);
    }
}
