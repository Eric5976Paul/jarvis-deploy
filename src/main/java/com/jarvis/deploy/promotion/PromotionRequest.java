package com.jarvis.deploy.promotion;

import java.util.Objects;

/**
 * Represents a request to promote a deployment artifact from one environment to another.
 */
public class PromotionRequest {

    private final String applicationName;
    private final String artifactVersion;
    private final String sourceEnvironment;
    private final String targetEnvironment;
    private final String requestedBy;

    public PromotionRequest(String applicationName, String artifactVersion,
                            String sourceEnvironment, String targetEnvironment,
                            String requestedBy) {
        Objects.requireNonNull(applicationName, "applicationName must not be null");
        Objects.requireNonNull(artifactVersion, "artifactVersion must not be null");
        Objects.requireNonNull(sourceEnvironment, "sourceEnvironment must not be null");
        Objects.requireNonNull(targetEnvironment, "targetEnvironment must not be null");
        Objects.requireNonNull(requestedBy, "requestedBy must not be null");
        if (sourceEnvironment.equalsIgnoreCase(targetEnvironment)) {
            throw new IllegalArgumentException("sourceEnvironment and targetEnvironment must differ");
        }
        this.applicationName = applicationName;
        this.artifactVersion = artifactVersion;
        this.sourceEnvironment = sourceEnvironment;
        this.targetEnvironment = targetEnvironment;
        this.requestedBy = requestedBy;
    }

    public String getApplicationName() { return applicationName; }
    public String getArtifactVersion() { return artifactVersion; }
    public String getSourceEnvironment() { return sourceEnvironment; }
    public String getTargetEnvironment() { return targetEnvironment; }
    public String getRequestedBy() { return requestedBy; }

    @Override
    public String toString() {
        return String.format("PromotionRequest{app='%s', version='%s', %s -> %s, by='%s'}",
                applicationName, artifactVersion, sourceEnvironment, targetEnvironment, requestedBy);
    }
}
