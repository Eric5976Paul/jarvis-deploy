package com.jarvis.deploy.circuit;

/**
 * Represents the possible states of a deployment circuit breaker.
 */
public enum CircuitBreakerState {

    /**
     * Circuit is closed — deployments are allowed to proceed normally.
     */
    CLOSED,

    /**
     * Circuit is open — deployments are blocked due to repeated failures.
     */
    OPEN,

    /**
     * Circuit is half-open — a single trial deployment is allowed to test recovery.
     */
    HALF_OPEN
}
