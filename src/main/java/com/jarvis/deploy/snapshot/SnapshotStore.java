package com.jarvis.deploy.snapshot;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * In-memory store for deployment snapshots, keyed by environment.
 * Supports retrieval of the latest snapshot and listing history per environment.
 */
public class SnapshotStore {

    private static final int MAX_SNAPSHOTS_PER_ENV = 10;

    private final Map<String, List<DeploymentSnapshot>> store = new ConcurrentHashMap<>();

    public void save(DeploymentSnapshot snapshot) {
        store.compute(snapshot.getEnvironment(), (env, snapshots) -> {
            if (snapshots == null) {
                snapshots = new ArrayList<>();
            }
            snapshots.add(snapshot);
            if (snapshots.size() > MAX_SNAPSHOTS_PER_ENV) {
                snapshots.sort(Comparator.comparing(DeploymentSnapshot::getCapturedAt));
                snapshots.remove(0);
            }
            return snapshots;
        });
    }

    public Optional<DeploymentSnapshot> getLatest(String environment) {
        List<DeploymentSnapshot> snapshots = store.getOrDefault(environment, List.of());
        return snapshots.stream()
                .max(Comparator.comparing(DeploymentSnapshot::getCapturedAt));
    }

    public Optional<DeploymentSnapshot> findById(String snapshotId) {
        return store.values().stream()
                .flatMap(List::stream)
                .filter(s -> s.getSnapshotId().equals(snapshotId))
                .findFirst();
    }

    public List<DeploymentSnapshot> listByEnvironment(String environment) {
        return store.getOrDefault(environment, List.of()).stream()
                .sorted(Comparator.comparing(DeploymentSnapshot::getCapturedAt).reversed())
                .collect(Collectors.toList());
    }

    public int countByEnvironment(String environment) {
        return store.getOrDefault(environment, List.of()).size();
    }

    public void clear(String environment) {
        store.remove(environment);
    }
}
