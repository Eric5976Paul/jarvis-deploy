package com.jarvis.deploy.badge;

import java.time.Instant;
import java.util.Objects;

/**
 * Represents a status badge for a deployment, summarizing its current state
 * in a compact, shareable format (e.g., for README shields or dashboards).
 */
public class DeploymentBadge {

    private final String environment;
    private final String version;
    private final BadgeStatus status;
    private final Instant generatedAt;
    private final String color;

    public DeploymentBadge(String environment, String version, BadgeStatus status, Instant generatedAt) {
        this.environment = Objects.requireNonNull(environment, "environment must not be null");
        this.version = Objects.requireNonNull(version, "version must not be null");
        this.status = Objects.requireNonNull(status, "status must not be null");
        this.generatedAt = Objects.requireNonNull(generatedAt, "generatedAt must not be null");
        this.color = resolveColor(status);
    }

    private String resolveColor(BadgeStatus status) {
        switch (status) {
            case SUCCESS: return "brightgreen";
            case FAILED:  return "red";
            case IN_PROGRESS: return "yellow";
            case PENDING: return "lightgrey";
            case ROLLED_BACK: return "orange";
            default: return "blue";
        }
    }

    public String getEnvironment() { return environment; }
    public String getVersion()     { return version; }
    public BadgeStatus getStatus() { return status; }
    public Instant getGeneratedAt(){ return generatedAt; }
    public String getColor()       { return color; }

    /**
     * Renders a simple Shields.io-compatible URL for embedding in READMEs.
     */
    public String toShieldsUrl() {
        String label = environment.replace("-", "--").replace("_", "__");
        String message = version.replace("-", "--").replace("_", "__");
        return String.format("https://img.shields.io/badge/%s-%s-%s", label, message, color);
    }

    @Override
    public String toString() {
        return String.format("DeploymentBadge{env='%s', version='%s', status=%s, color='%s', generatedAt=%s}",
                environment, version, status, color, generatedAt);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DeploymentBadge)) return false;
        DeploymentBadge that = (DeploymentBadge) o;
        return Objects.equals(environment, that.environment)
                && Objects.equals(version, that.version)
                && status == that.status;
    }

    @Override
    public int hashCode() {
        return Objects.hash(environment, version, status);
    }
}
