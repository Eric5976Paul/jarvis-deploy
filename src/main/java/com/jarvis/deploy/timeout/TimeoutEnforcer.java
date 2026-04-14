package com.jarvis.deploy.timeout;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages and enforces deployment timeouts across active deployments.
 */
public class TimeoutEnforcer {

    private final Map<String, DeploymentTimeout> activeTimeouts = new ConcurrentHashMap<>();

    /**
     * Registers a timeout for the given deployment.
     *
     * @param deploymentId the deployment identifier
     * @param maxDuration  the maximum allowed duration
     * @return the registered {@link DeploymentTimeout}
     */
    public DeploymentTimeout register(String deploymentId, Duration maxDuration) {
        DeploymentTimeout timeout = new DeploymentTimeout(deploymentId, maxDuration);
        activeTimeouts.put(deploymentId, timeout);
        return timeout;
    }

    /**
     * Checks whether the deployment with the given ID has exceeded its timeout.
     *
     * @param deploymentId the deployment identifier
     * @return {@code true} if the timeout has been exceeded, {@code false} otherwise
     * @throws IllegalArgumentException if no timeout is registered for the deployment
     */
    public boolean isExceeded(String deploymentId) {
        DeploymentTimeout timeout = requireTimeout(deploymentId);
        return timeout.isExceeded();
    }

    /**
     * Returns the remaining time for the given deployment's timeout.
     *
     * @param deploymentId the deployment identifier
     * @return remaining {@link Duration}, or {@link Duration#ZERO} if already exceeded
     */
    public Duration getRemaining(String deploymentId) {
        return requireTimeout(deploymentId).getRemaining();
    }

    /**
     * Removes the timeout registration for the given deployment.
     *
     * @param deploymentId the deployment identifier
     * @return the removed timeout, or empty if none was registered
     */
    public Optional<DeploymentTimeout> release(String deploymentId) {
        return Optional.ofNullable(activeTimeouts.remove(deploymentId));
    }

    /**
     * Returns the number of currently tracked deployments.
     */
    public int activeCount() {
        return activeTimeouts.size();
    }

    public Optional<DeploymentTimeout> find(String deploymentId) {
        return Optional.ofNullable(activeTimeouts.get(deploymentId));
    }

    private DeploymentTimeout requireTimeout(String deploymentId) {
        DeploymentTimeout timeout = activeTimeouts.get(deploymentId);
        if (timeout == null) {
            throw new IllegalArgumentException("No timeout registered for deployment: " + deploymentId);
        }
        return timeout;
    }
}
