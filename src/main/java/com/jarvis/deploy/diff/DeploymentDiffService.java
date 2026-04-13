package com.jarvis.deploy.diff;

import com.jarvis.deploy.snapshot.DeploymentSnapshot;
import com.jarvis.deploy.snapshot.SnapshotStore;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.HashSet;

/**
 * Service for computing diffs between deployment snapshots.
 */
public class DeploymentDiffService {

    private final SnapshotStore snapshotStore;

    public DeploymentDiffService(SnapshotStore snapshotStore) {
        this.snapshotStore = snapshotStore;
    }

    public Optional<DeploymentDiff> diff(String environment, String fromVersion, String toVersion) {
        Optional<DeploymentSnapshot> fromSnap = snapshotStore.findByVersion(environment, fromVersion);
        Optional<DeploymentSnapshot> toSnap = snapshotStore.findByVersion(environment, toVersion);

        if (fromSnap.isEmpty() || toSnap.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(computeDiff(fromSnap.get(), toSnap.get(), environment, fromVersion, toVersion));
    }

    private DeploymentDiff computeDiff(DeploymentSnapshot from, DeploymentSnapshot to,
                                       String environment, String fromVersion, String toVersion) {
        DeploymentDiff diff = new DeploymentDiff(fromVersion, toVersion, environment);

        Map<String, String> fromProps = from.getProperties();
        Map<String, String> toProps = to.getProperties();

        Set<String> allKeys = new HashSet<>();
        allKeys.addAll(fromProps.keySet());
        allKeys.addAll(toProps.keySet());

        for (String key : allKeys) {
            String oldVal = fromProps.get(key);
            String newVal = toProps.get(key);
            if (!java.util.Objects.equals(oldVal, newVal)) {
                diff.addChange(key, oldVal, newVal);
            }
        }

        return diff;
    }
}
