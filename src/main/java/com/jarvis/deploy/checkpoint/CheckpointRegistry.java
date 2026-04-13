package com.jarvis.deploy.checkpoint;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * In-memory registry that stores and queries {@link DeploymentCheckpoint} entries
 * keyed by deployment ID.
 */
public class CheckpointRegistry {

    private final Map<String, List<DeploymentCheckpoint>> store = new ConcurrentHashMap<>();

    /**
     * Records a checkpoint for a deployment.
     */
    public void record(DeploymentCheckpoint checkpoint) {
        if (checkpoint == null) {
            throw new IllegalArgumentException("checkpoint must not be null");
        }
        store.computeIfAbsent(checkpoint.getDeploymentId(), id -> new ArrayList<>())
             .add(checkpoint);
    }

    /**
     * Returns all checkpoints for the given deployment ID, in insertion order.
     */
    public List<DeploymentCheckpoint> getCheckpoints(String deploymentId) {
        return Collections.unmodifiableList(
                store.getOrDefault(deploymentId, Collections.emptyList()));
    }

    /**
     * Returns true only if every recorded checkpoint for the deployment passed.
     */
    public boolean allPassed(String deploymentId) {
        List<DeploymentCheckpoint> checkpoints = getCheckpoints(deploymentId);
        if (checkpoints.isEmpty()) return false;
        return checkpoints.stream().allMatch(DeploymentCheckpoint::isPassed);
    }

    /**
     * Returns checkpoints that failed for a given deployment.
     */
    public List<DeploymentCheckpoint> getFailedCheckpoints(String deploymentId) {
        return getCheckpoints(deploymentId).stream()
                .filter(c -> c.getStatus() == CheckpointStatus.FAILED)
                .collect(Collectors.toList());
    }

    /**
     * Clears all checkpoints for a deployment (e.g. on retry).
     */
    public void clear(String deploymentId) {
        store.remove(deploymentId);
    }

    public int totalCheckpoints(String deploymentId) {
        return getCheckpoints(deploymentId).size();
    }
}
