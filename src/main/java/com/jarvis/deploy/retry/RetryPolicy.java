package com.jarvis.deploy.retry;

import java.time.Duration;

/**
 * Defines retry behavior for deployment operations.
 */
public class RetryPolicy {

    private final int maxAttempts;
    private final Duration initialDelay;
    private final double backoffMultiplier;
    private final Duration maxDelay;

    public RetryPolicy(int maxAttempts, Duration initialDelay, double backoffMultiplier, Duration maxDelay) {
        if (maxAttempts < 1) throw new IllegalArgumentException("maxAttempts must be >= 1");
        if (backoffMultiplier < 1.0) throw new IllegalArgumentException("backoffMultiplier must be >= 1.0");
        this.maxAttempts = maxAttempts;
        this.initialDelay = initialDelay;
        this.backoffMultiplier = backoffMultiplier;
        this.maxDelay = maxDelay;
    }

    public static RetryPolicy noRetry() {
        return new RetryPolicy(1, Duration.ZERO, 1.0, Duration.ZERO);
    }

    public static RetryPolicy defaultPolicy() {
        return new RetryPolicy(3, Duration.ofSeconds(2), 2.0, Duration.ofSeconds(30));
    }

    public int getMaxAttempts() {
        return maxAttempts;
    }

    public Duration getDelayForAttempt(int attempt) {
        if (attempt <= 1) return initialDelay;
        long millis = (long) (initialDelay.toMillis() * Math.pow(backoffMultiplier, attempt - 1));
        return Duration.ofMillis(Math.min(millis, maxDelay.toMillis()));
    }

    public boolean shouldRetry(int attemptNumber) {
        return attemptNumber < maxAttempts;
    }

    public double getBackoffMultiplier() {
        return backoffMultiplier;
    }

    public Duration getMaxDelay() {
        return maxDelay;
    }

    public Duration getInitialDelay() {
        return initialDelay;
    }

    @Override
    public String toString() {
        return String.format("RetryPolicy{maxAttempts=%d, initialDelay=%s, backoffMultiplier=%.1f, maxDelay=%s}",
                maxAttempts, initialDelay, backoffMultiplier, maxDelay);
    }
}
