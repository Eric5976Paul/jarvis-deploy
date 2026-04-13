package com.jarvis.deploy.drain;

import java.time.Duration;
import java.time.Instant;

/**
 * Encapsulates the result of a drain operation.
 */
public class DrainResult {

    public enum Status { DRAINED, TIMED_OUT, FORCED, FAILED }

    private final Status status;
    private final String environment;
    private final String appName;
    private final Duration elapsed;
    private final String message;
    private final Instant completedAt;

    private DrainResult(Status status, String environment, String appName,
                        Duration elapsed, String message) {
        this.status = status;
        this.environment = environment;
        this.appName = appName;
        this.elapsed = elapsed;
        this.message = message;
        this.completedAt = Instant.now();
    }

    public static DrainResult drained(String environment, String appName, Duration elapsed) {
        return new DrainResult(Status.DRAINED, environment, appName, elapsed,
                "Drain completed successfully.");
    }

    public static DrainResult timedOut(String environment, String appName, Duration elapsed) {
        return new DrainResult(Status.TIMED_OUT, environment, appName, elapsed,
                "Drain timed out after " + elapsed.toSeconds() + "s.");
    }

    public static DrainResult forced(String environment, String appName, Duration elapsed) {
        return new DrainResult(Status.FORCED, environment, appName, elapsed,
                "Drain timed out; force-stopped after " + elapsed.toSeconds() + "s.");
    }

    public static DrainResult failed(String environment, String appName, String reason) {
        return new DrainResult(Status.FAILED, environment, appName, Duration.ZERO, reason);
    }

    public boolean isSuccessful() {
        return status == Status.DRAINED || status == Status.FORCED;
    }

    public Status getStatus() { return status; }
    public String getEnvironment() { return environment; }
    public String getAppName() { return appName; }
    public Duration getElapsed() { return elapsed; }
    public String getMessage() { return message; }
    public Instant getCompletedAt() { return completedAt; }

    @Override
    public String toString() {
        return "DrainResult{status=" + status + ", app='" + appName +
                "', env='" + environment + "', elapsed=" + elapsed + "}";
    }
}
