package com.jarvis.deploy.stealth;

import java.time.Instant;
import java.util.Objects;

/**
 * Represents a stealth mode configuration for a deployment,
 * suppressing notifications and audit events during sensitive operations.
 */
public class StealthMode {

    private final String deploymentId;
    private final String environment;
    private final boolean suppressNotifications;
    private final boolean suppressAudit;
    private final String reason;
    private final Instant activatedAt;
    private final Instant expiresAt;

    public StealthMode(String deploymentId, String environment,
                       boolean suppressNotifications, boolean suppressAudit,
                       String reason, Instant activatedAt, Instant expiresAt) {
        this.deploymentId = Objects.requireNonNull(deploymentId, "deploymentId must not be null");
        this.environment = Objects.requireNonNull(environment, "environment must not be null");
        this.suppressNotifications = suppressNotifications;
        this.suppressAudit = suppressAudit;
        this.reason = reason;
        this.activatedAt = Objects.requireNonNull(activatedAt, "activatedAt must not be null");
        this.expiresAt = expiresAt;
    }

    public String getDeploymentId() { return deploymentId; }
    public String getEnvironment() { return environment; }
    public boolean isSuppressNotifications() { return suppressNotifications; }
    public boolean isSuppressAudit() { return suppressAudit; }
    public String getReason() { return reason; }
    public Instant getActivatedAt() { return activatedAt; }
    public Instant getExpiresAt() { return expiresAt; }

    public boolean isExpired(Instant now) {
        return expiresAt != null && now.isAfter(expiresAt);
    }

    public boolean isActive(Instant now) {
        return !isExpired(now);
    }

    @Override
    public String toString() {
        return "StealthMode{deploymentId='" + deploymentId + "', environment='" + environment +
               "', suppressNotifications=" + suppressNotifications +
               ", suppressAudit=" + suppressAudit + ", expiresAt=" + expiresAt + "}";
    }
}
