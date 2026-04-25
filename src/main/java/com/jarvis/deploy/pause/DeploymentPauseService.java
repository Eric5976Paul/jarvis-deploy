package com.jarvis.deploy.pause;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages pausing and resuming deployments mid-flight.
 * A paused deployment will not proceed to the next pipeline stage
 * until explicitly resumed or the pause expires.
 */
public class DeploymentPauseService {

    private final Map<String, PauseRecord> pausedDeployments = new ConcurrentHashMap<>();

    /**
     * Pauses the deployment identified by deploymentId.
     *
     * @param deploymentId unique deployment identifier
     * @param reason       human-readable reason for the pause
     * @param pausedBy     actor who initiated the pause
     * @param expiresAt    optional expiry; null means indefinite
     * @return the resulting PauseRecord
     */
    public PauseRecord pause(String deploymentId, String reason, String pausedBy, Instant expiresAt) {
        if (deploymentId == null || deploymentId.isBlank()) {
            throw new IllegalArgumentException("deploymentId must not be blank");
        }
        PauseRecord record = new PauseRecord(deploymentId, reason, pausedBy, Instant.now(), expiresAt);
        pausedDeployments.put(deploymentId, record);
        return record;
    }

    /**
     * Resumes a previously paused deployment.
     *
     * @param deploymentId unique deployment identifier
     * @return true if the deployment was paused and is now resumed; false if it was not paused
     */
    public boolean resume(String deploymentId) {
        return pausedDeployments.remove(deploymentId) != null;
    }

    /**
     * Checks whether a deployment is currently paused.
     * Automatically clears expired pauses.
     *
     * @param deploymentId unique deployment identifier
     * @return true if the deployment is actively paused
     */
    public boolean isPaused(String deploymentId) {
        PauseRecord record = pausedDeployments.get(deploymentId);
        if (record == null) {
            return false;
        }
        if (record.expiresAt() != null && Instant.now().isAfter(record.expiresAt())) {
            pausedDeployments.remove(deploymentId);
            return false;
        }
        return true;
    }

    /**
     * Returns the PauseRecord for a deployment if it is currently paused.
     *
     * @param deploymentId unique deployment identifier
     * @return an Optional containing the record, or empty if not paused
     */
    public Optional<PauseRecord> getPauseRecord(String deploymentId) {
        if (!isPaused(deploymentId)) {
            return Optional.empty();
        }
        return Optional.ofNullable(pausedDeployments.get(deploymentId));
    }

    /** Returns the number of currently paused deployments. */
    public int pausedCount() {
        return pausedDeployments.size();
    }
}
