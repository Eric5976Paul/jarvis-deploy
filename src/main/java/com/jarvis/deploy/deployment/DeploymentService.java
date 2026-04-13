package com.jarvis.deploy.deployment;

import com.jarvis.deploy.config.DeploymentConfig;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Orchestrates deployment and rollback operations for a given environment.
 */
public class DeploymentService {

    private final DeploymentHistory history;
    private final DeploymentConfig config;

    public DeploymentService(DeploymentConfig config, DeploymentHistory history) {
        this.config = config;
        this.history = history;
    }

    public boolean deploy(String environment, String version, String artifactPath) {
        System.out.printf("Deploying version %s to environment '%s'...%n", version, environment);
        DeploymentRecord.DeploymentStatus status;
        try {
            runDeploy(environment, artifactPath);
            status = DeploymentRecord.DeploymentStatus.SUCCESS;
            System.out.println("Deployment succeeded.");
        } catch (Exception e) {
            status = DeploymentRecord.DeploymentStatus.FAILED;
            System.err.println("Deployment failed: " + e.getMessage());
        }
        history.record(new DeploymentRecord(environment, version, artifactPath,
                LocalDateTime.now(), status));
        return status == DeploymentRecord.DeploymentStatus.SUCCESS;
    }

    public boolean rollback(String environment) {
        List<DeploymentRecord> envHistory = history.getHistory(environment);
        Optional<DeploymentRecord> target = envHistory.stream()
                .filter(r -> r.getStatus() == DeploymentRecord.DeploymentStatus.SUCCESS)
                .skip(1)
                .findFirst();

        if (target.isEmpty()) {
            System.err.println("No previous successful deployment found for: " + environment);
            return false;
        }

        DeploymentRecord rollbackTarget = target.get();
        System.out.printf("Rolling back '%s' to version %s...%n",
                environment, rollbackTarget.getVersion());
        try {
            runDeploy(environment, rollbackTarget.getArtifactPath());
            history.record(new DeploymentRecord(environment, rollbackTarget.getVersion(),
                    rollbackTarget.getArtifactPath(), LocalDateTime.now(),
                    DeploymentRecord.DeploymentStatus.ROLLED_BACK));
            System.out.println("Rollback succeeded.");
            return true;
        } catch (Exception e) {
            System.err.println("Rollback failed: " + e.getMessage());
            return false;
        }
    }

    public void printHistory(String environment) {
        List<DeploymentRecord> records = history.getHistory(environment);
        if (records.isEmpty()) {
            System.out.println("No deployment history for: " + environment);
            return;
        }
        System.out.println("Deployment history for '" + environment + "':");
        records.forEach(r -> System.out.println("  " + r));
    }

    private void runDeploy(String environment, String artifactPath) throws Exception {
        // Placeholder: actual deploy logic (e.g., SSH copy + restart) would go here
        if (artifactPath == null || artifactPath.isBlank()) {
            throw new IllegalArgumentException("Artifact path must not be empty.");
        }
    }
}
