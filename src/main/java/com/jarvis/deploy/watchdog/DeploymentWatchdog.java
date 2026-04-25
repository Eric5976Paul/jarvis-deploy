package com.jarvis.deploy.watchdog;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Monitors active deployments and detects stalled or hung deployments
 * that have exceeded their expected duration without completing.
 */
public class DeploymentWatchdog {

    private final Duration defaultStallThreshold;
    private final Map<String, WatchdogEntry> watched = new ConcurrentHashMap<>();

    public DeploymentWatchdog(Duration defaultStallThreshold) {
        if (defaultStallThreshold == null || defaultStallThreshold.isNegative()) {
            throw new IllegalArgumentException("Stall threshold must be a positive duration");
        }
        this.defaultStallThreshold = defaultStallThreshold;
    }

    public void watch(String deploymentId, String environment) {
        watch(deploymentId, environment, defaultStallThreshold);
    }

    public void watch(String deploymentId, String environment, Duration stallThreshold) {
        if (deploymentId == null || deploymentId.isBlank()) {
            throw new IllegalArgumentException("Deployment ID must not be blank");
        }
        watched.put(deploymentId, new WatchdogEntry(deploymentId, environment, Instant.now(), stallThreshold));
    }

    public void unwatch(String deploymentId) {
        watched.remove(deploymentId);
    }

    public WatchdogCheckResult check(String deploymentId) {
        WatchdogEntry entry = watched.get(deploymentId);
        if (entry == null) {
            return WatchdogCheckResult.notWatched(deploymentId);
        }
        Duration elapsed = Duration.between(entry.getStartedAt(), Instant.now());
        boolean stalled = elapsed.compareTo(entry.getStallThreshold()) > 0;
        return stalled
                ? WatchdogCheckResult.stalled(deploymentId, entry.getEnvironment(), elapsed)
                : WatchdogCheckResult.healthy(deploymentId, entry.getEnvironment(), elapsed);
    }

    public Map<String, WatchdogCheckResult> checkAll() {
        Map<String, WatchdogCheckResult> results = new ConcurrentHashMap<>();
        watched.keySet().forEach(id -> results.put(id, check(id)));
        return results;
    }

    public int watchedCount() {
        return watched.size();
    }

    public boolean isWatched(String deploymentId) {
        return watched.containsKey(deploymentId);
    }
}
