package com.jarvis.deploy.concurrency;

import java.util.Objects;

/**
 * Defines the concurrency policy for deployments in a given environment.
 * Controls how many simultaneous deployments are permitted and what
 * happens when the limit is exceeded.
 */
public class ConcurrencyPolicy {

    private final String environment;
    private final int maxConcurrentDeployments;
    private final ConcurrencyViolationAction violationAction;
    private final long queueTimeoutSeconds;

    public ConcurrencyPolicy(String environment, int maxConcurrentDeployments,
                             ConcurrencyViolationAction violationAction, long queueTimeoutSeconds) {
        if (environment == null || environment.isBlank()) {
            throw new IllegalArgumentException("Environment must not be blank");
        }
        if (maxConcurrentDeployments < 1) {
            throw new IllegalArgumentException("maxConcurrentDeployments must be >= 1");
        }
        if (queueTimeoutSeconds < 0) {
            throw new IllegalArgumentException("queueTimeoutSeconds must be >= 0");
        }
        this.environment = environment;
        this.maxConcurrentDeployments = maxConcurrentDeployments;
        this.violationAction = Objects.requireNonNull(violationAction, "violationAction must not be null");
        this.queueTimeoutSeconds = queueTimeoutSeconds;
    }

    public static ConcurrencyPolicy defaultPolicy(String environment) {
        return new ConcurrencyPolicy(environment, 1, ConcurrencyViolationAction.REJECT, 0);
    }

    public String getEnvironment() { return environment; }
    public int getMaxConcurrentDeployments() { return maxConcurrentDeployments; }
    public ConcurrencyViolationAction getViolationAction() { return violationAction; }
    public long getQueueTimeoutSeconds() { return queueTimeoutSeconds; }

    @Override
    public String toString() {
        return String.format("ConcurrencyPolicy[env=%s, max=%d, action=%s, queueTimeout=%ds]",
                environment, maxConcurrentDeployments, violationAction, queueTimeoutSeconds);
    }
}
