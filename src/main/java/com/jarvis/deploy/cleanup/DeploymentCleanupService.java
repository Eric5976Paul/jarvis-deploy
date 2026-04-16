package com.jarvis.deploy.cleanup;

import com.jarvis.deploy.deployment.DeploymentHistory;
import com.jarvis.deploy.deployment.DeploymentRecord;
import com.jarvis.deploy.snapshot.SnapshotStore;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * Service responsible for cleaning up stale deployment records and snapshots
 * beyond a configured retention period.
 */
public class DeploymentCleanupService {

    private final DeploymentHistory deploymentHistory;
    private final SnapshotStore snapshotStore;
    private final int retentionDays;

    public DeploymentCleanupService(DeploymentHistory deploymentHistory,
                                    SnapshotStore snapshotStore,
                                    int retentionDays) {
        if (retentionDays <= 0) {
            throw new IllegalArgumentException("retentionDays must be positive");
        }
        this.deploymentHistory = deploymentHistory;
        this.snapshotStore = snapshotStore;
        this.retentionDays = retentionDays;
    }

    public CleanupResult cleanup(String environment) {
        Instant cutoff = Instant.now().minus(retentionDays, ChronoUnit.DAYS);
        List<String> removedRecords = new ArrayList<>();
        List<String> removedSnapshots = new ArrayList<>();

        List<DeploymentRecord> records = deploymentHistory.getRecordsForEnvironment(environment);
        for (DeploymentRecord record : records) {
            if (record.getTimestamp().isBefore(cutoff)) {
                boolean removed = deploymentHistory.removeRecord(record.getDeploymentId());
                if (removed) {
                    removedRecords.add(record.getDeploymentId());
                    boolean snapRemoved = snapshotStore.deleteSnapshot(record.getDeploymentId());
                    if (snapRemoved) {
                        removedSnapshots.add(record.getDeploymentId());
                    }
                }
            }
        }

        return new CleanupResult(environment, removedRecords, removedSnapshots, cutoff);
    }

    public int getRetentionDays() {
        return retentionDays;
    }
}
