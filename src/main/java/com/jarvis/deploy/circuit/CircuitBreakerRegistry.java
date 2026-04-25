package com.jarvis.deploy.circuit;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry that manages per-environment {@link DeploymentCircuitBreaker} instances.
 */
public class CircuitBreakerRegistry {

    private static final int DEFAULT_FAILURE_THRESHOLD = 3;
    private static final Duration DEFAULT_OPEN_DURATION = Duration.ofMinutes(5);

    private final Map<String, DeploymentCircuitBreaker> breakers = new ConcurrentHashMap<>();
    private final int defaultFailureThreshold;
    private final Duration defaultOpenDuration;

    public CircuitBreakerRegistry() {
        this(DEFAULT_FAILURE_THRESHOLD, DEFAULT_OPEN_DURATION);
    }

    public CircuitBreakerRegistry(int defaultFailureThreshold, Duration defaultOpenDuration) {
        this.defaultFailureThreshold = defaultFailureThreshold;
        this.defaultOpenDuration = defaultOpenDuration;
    }

    /**
     * Returns the circuit breaker for the given environment, creating one with
     * default settings if it does not already exist.
     */
    public DeploymentCircuitBreaker getOrCreate(String environment) {
        return breakers.computeIfAbsent(environment,
                env -> new DeploymentCircuitBreaker(env, defaultFailureThreshold, defaultOpenDuration));
    }

    public Optional<DeploymentCircuitBreaker> get(String environment) {
        return Optional.ofNullable(breakers.get(environment));
    }

    public void register(DeploymentCircuitBreaker breaker) {
        breakers.put(breaker.getEnvironment(), breaker);
    }

    public boolean remove(String environment) {
        return breakers.remove(environment) != null;
    }

    public void resetAll() {
        breakers.values().forEach(DeploymentCircuitBreaker::reset);
    }

    public Map<String, DeploymentCircuitBreaker> all() {
        return Collections.unmodifiableMap(breakers);
    }

    public int size() {
        return breakers.size();
    }
}
