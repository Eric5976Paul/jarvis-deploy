package com.jarvis.deploy.eviction;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Service responsible for evicting stale or expired deployment entries
 * from active tracking based on configurable eviction policies.
 */
public class DeploymentEvictionService {

    private final Map<String, EvictionEntry> tracked = new ConcurrentHashMap<>();
    private final long defaultTtlSeconds;

    public DeploymentEvictionService(long defaultTtlSeconds) {
        if (defaultTtlSeconds <= 0) {
            throw new IllegalArgumentException("TTL must be positive");
        }
        this.defaultTtlSeconds = defaultTtlSeconds;
    }

    public void track(String deploymentId, String environment) {
        track(deploymentId, environment, defaultTtlSeconds);
    }

    public void track(String deploymentId, String environment, long ttlSeconds) {
        if (deploymentId == null || deploymentId.isBlank()) {
            throw new IllegalArgumentException("deploymentId must not be blank");
        }
        Instant expiresAt = Instant.now().plusSeconds(ttlSeconds);
        tracked.put(deploymentId, new EvictionEntry(deploymentId, environment, expiresAt));
    }

    public EvictionResult evictExpired() {
        Instant now = Instant.now();
        List<String> evicted = new ArrayList<>();

        tracked.entrySet().removeIf(entry -> {
            boolean expired = entry.getValue().getExpiresAt().isBefore(now);
            if (expired) {
                evicted.add(entry.getKey());
            }
            return expired;
        });

        return new EvictionResult(evicted, now);
    }

    public EvictionResult evictByEnvironment(String environment) {
        Instant now = Instant.now();
        List<String> evicted = tracked.entrySet().stream()
                .filter(e -> environment.equals(e.getValue().getEnvironment()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        evicted.forEach(tracked::remove);
        return new EvictionResult(evicted, now);
    }

    public boolean isTracked(String deploymentId) {
        return tracked.containsKey(deploymentId);
    }

    public int trackedCount() {
        return tracked.size();
    }

    public void untrack(String deploymentId) {
        tracked.remove(deploymentId);
    }
}
