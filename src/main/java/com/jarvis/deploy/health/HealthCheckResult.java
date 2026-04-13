package com.jarvis.deploy.health;

import java.time.Duration;

/**
 * Immutable value object representing the outcome of a health check poll.
 */
public final class HealthCheckResult {

    public enum Status { SUCCESS, FAILURE }

    private final Status status;
    private final String url;
    private final Duration elapsed;
    private final int attemptsUsed;

    private HealthCheckResult(Status status, String url, Duration elapsed, int attemptsUsed) {
        this.status = status;
        this.url = url;
        this.elapsed = elapsed;
        this.attemptsUsed = attemptsUsed;
    }

    public static HealthCheckResult success(String url, Duration elapsed) {
        return new HealthCheckResult(Status.SUCCESS, url, elapsed, 1);
    }

    public static HealthCheckResult failure(String url, Duration elapsed, int attemptsUsed) {
        return new HealthCheckResult(Status.FAILURE, url, elapsed, attemptsUsed);
    }

    public boolean isHealthy() {
        return status == Status.SUCCESS;
    }

    public Status getStatus() {
        return status;
    }

    public String getUrl() {
        return url;
    }

    public Duration getElapsed() {
        return elapsed;
    }

    public int getAttemptsUsed() {
        return attemptsUsed;
    }

    @Override
    public String toString() {
        return String.format("HealthCheckResult{status=%s, url='%s', elapsed=%dms, attempts=%d}",
                status, url, elapsed.toMillis(), attemptsUsed);
    }
}
