package com.jarvis.deploy.concurrency;

import java.time.Instant;

/**
 * Represents the outcome of attempting to acquire a concurrency slot
 * for a deployment in a specific environment.
 */
public class ConcurrencyAcquireResult {

    private final boolean acquired;
    private final String environment;
    private final String message;
    private final Instant timestamp;

    private ConcurrencyAcquireResult(boolean acquired, String environment, String message) {
        this.acquired = acquired;
        this.environment = environment;
        this.message = message;
        this.timestamp = Instant.now();
    }

    public static ConcurrencyAcquireResult acquired(String environment) {
        return new ConcurrencyAcquireResult(true, environment,
                "Deployment slot acquired for environment: " + environment);
    }

    public static ConcurrencyAcquireResult rejected(String environment, String reason) {
        return new ConcurrencyAcquireResult(false, environment, reason);
    }

    public boolean isAcquired() { return acquired; }
    public String getEnvironment() { return environment; }
    public String getMessage() { return message; }
    public Instant getTimestamp() { return timestamp; }

    @Override
    public String toString() {
        return String.format("ConcurrencyAcquireResult[acquired=%b, env=%s, message='%s']",
                acquired, environment, message);
    }
}
