package com.jarvis.deploy.manifest;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Represents a deployment manifest capturing all configuration and metadata
 * needed to reproduce or audit a specific deployment.
 */
public class DeploymentManifest {

    private final String manifestId;
    private final String environment;
    private final String artifactId;
    private final String version;
    private final Map<String, String> parameters;
    private final Instant createdAt;
    private final String createdBy;
    private ManifestStatus status;

    public DeploymentManifest(String manifestId, String environment, String artifactId,
                               String version, Map<String, String> parameters, String createdBy) {
        this.manifestId = Objects.requireNonNull(manifestId, "manifestId must not be null");
        this.environment = Objects.requireNonNull(environment, "environment must not be null");
        this.artifactId = Objects.requireNonNull(artifactId, "artifactId must not be null");
        this.version = Objects.requireNonNull(version, "version must not be null");
        this.parameters = Collections.unmodifiableMap(new HashMap<>(parameters != null ? parameters : Collections.emptyMap()));
        this.createdBy = Objects.requireNonNull(createdBy, "createdBy must not be null");
        this.createdAt = Instant.now();
        this.status = ManifestStatus.DRAFT;
    }

    public String getManifestId() { return manifestId; }
    public String getEnvironment() { return environment; }
    public String getArtifactId() { return artifactId; }
    public String getVersion() { return version; }
    public Map<String, String> getParameters() { return parameters; }
    public Instant getCreatedAt() { return createdAt; }
    public String getCreatedBy() { return createdBy; }
    public ManifestStatus getStatus() { return status; }

    public void seal() {
        if (this.status != ManifestStatus.DRAFT) {
            throw new IllegalStateException("Only DRAFT manifests can be sealed");
        }
        this.status = ManifestStatus.SEALED;
    }

    public void archive() {
        this.status = ManifestStatus.ARCHIVED;
    }

    public boolean isSealed() {
        return this.status == ManifestStatus.SEALED;
    }

    @Override
    public String toString() {
        return "DeploymentManifest{" +
                "manifestId='" + manifestId + '\'' +
                ", environment='" + environment + '\'' +
                ", artifactId='" + artifactId + '\'' +
                ", version='" + version + '\'' +
                ", status=" + status +
                '}';
    }
}
