package com.jarvis.deploy.deployment;

import java.time.LocalDateTime;

/**
 * Represents a single deployment record for rollback tracking.
 */
public class DeploymentRecord {

    private final String environment;
    private final String version;
    private final String artifactPath;
    private final LocalDateTime deployedAt;
    private final DeploymentStatus status;

    public DeploymentRecord(String environment, String version, String artifactPath,
                            LocalDateTime deployedAt, DeploymentStatus status) {
        this.environment = environment;
        this.version = version;
        this.artifactPath = artifactPath;
        this.deployedAt = deployedAt;
        this.status = status;
    }

    public String getEnvironment() { return environment; }
    public String getVersion() { return version; }
    public String getArtifactPath() { return artifactPath; }
    public LocalDateTime getDeployedAt() { return deployedAt; }
    public DeploymentStatus getStatus() { return status; }

    @Override
    public String toString() {
        return String.format("[%s] env=%s version=%s artifact=%s status=%s",
                deployedAt, environment, version, artifactPath, status);
    }

    public enum DeploymentStatus {
        SUCCESS, FAILED, ROLLED_BACK
    }
}
