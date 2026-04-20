package com.jarvis.deploy.cooldown;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

/**
 * Represents a cooldown period enforced between deployments
 * for a given environment or application.
 */
public class DeploymentCooldown {

    private final String environment;
    private final String application;
    private final Duration cooldownDuration;
    private final Instant lastDeployedAt;

    public DeploymentCooldown(String environment, String application,
                              Duration cooldownDuration, Instant lastDeployedAt) {
        this.environment = Objects.requireNonNull(environment, "environment must not be null");
        this.application = Objects.requireNonNull(application, "application must not be null");
        this.cooldownDuration = Objects.requireNonNull(cooldownDuration, "cooldownDuration must not be null");
        this.lastDeployedAt = Objects.requireNonNull(lastDeployedAt, "lastDeployedAt must not be null");
    }

    public String getEnvironment() {
        return environment;
    }

    public String getApplication() {
        return application;
    }

    public Duration getCooldownDuration() {
        return cooldownDuration;
    }

    public Instant getLastDeployedAt() {
        return lastDeployedAt;
    }

    public Instant getCooldownExpiresAt() {
        return lastDeployedAt.plus(cooldownDuration);
    }

    public boolean isExpired(Instant now) {
        return now.isAfter(getCooldownExpiresAt());
    }

    public Duration remainingCooldown(Instant now) {
        Instant expiresAt = getCooldownExpiresAt();
        if (now.isAfter(expiresAt)) {
            return Duration.ZERO;
        }
        return Duration.between(now, expiresAt);
    }

    @Override
    public String toString() {
        return "DeploymentCooldown{" +
                "environment='" + environment + '\'' +
                ", application='" + application + '\'' +
                ", cooldownDuration=" + cooldownDuration +
                ", lastDeployedAt=" + lastDeployedAt +
                '}';
    }
}
