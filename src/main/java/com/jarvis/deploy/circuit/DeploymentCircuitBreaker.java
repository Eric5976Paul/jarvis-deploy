package com.jarvis.deploy.circuit;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Circuit breaker that prevents deployments from proceeding when repeated
 * failures have been detected, protecting downstream systems.
 */
public class DeploymentCircuitBreaker {

    private final String environment;
    private final int failureThreshold;
    private final Duration openDuration;

    private final AtomicInteger failureCount = new AtomicInteger(0);
    private final AtomicInteger successCount = new AtomicInteger(0);
    private final AtomicReference<CircuitBreakerState> state =
            new AtomicReference<>(CircuitBreakerState.CLOSED);
    private volatile Instant openedAt = null;

    public DeploymentCircuitBreaker(String environment, int failureThreshold, Duration openDuration) {
        if (environment == null || environment.isBlank()) {
            throw new IllegalArgumentException("Environment must not be blank");
        }
        if (failureThreshold < 1) {
            throw new IllegalArgumentException("Failure threshold must be at least 1");
        }
        this.environment = environment;
        this.failureThreshold = failureThreshold;
        this.openDuration = openDuration;
    }

    public CircuitBreakerState getState() {
        if (state.get() == CircuitBreakerState.OPEN && openedAt != null) {
            if (Duration.between(openedAt, Instant.now()).compareTo(openDuration) >= 0) {
                state.compareAndSet(CircuitBreakerState.OPEN, CircuitBreakerState.HALF_OPEN);
            }
        }
        return state.get();
    }

    public boolean allowDeployment() {
        CircuitBreakerState current = getState();
        return current == CircuitBreakerState.CLOSED || current == CircuitBreakerState.HALF_OPEN;
    }

    public void recordSuccess() {
        successCount.incrementAndGet();
        failureCount.set(0);
        state.set(CircuitBreakerState.CLOSED);
        openedAt = null;
    }

    public void recordFailure() {
        int failures = failureCount.incrementAndGet();
        if (failures >= failureThreshold) {
            if (state.compareAndSet(CircuitBreakerState.CLOSED, CircuitBreakerState.OPEN)
                    || state.compareAndSet(CircuitBreakerState.HALF_OPEN, CircuitBreakerState.OPEN)) {
                openedAt = Instant.now();
            }
        }
    }

    public void reset() {
        failureCount.set(0);
        successCount.set(0);
        state.set(CircuitBreakerState.CLOSED);
        openedAt = null;
    }

    public int getFailureCount() { return failureCount.get(); }
    public int getSuccessCount() { return successCount.get(); }
    public String getEnvironment() { return environment; }
    public int getFailureThreshold() { return failureThreshold; }
    public Duration getOpenDuration() { return openDuration; }
}
