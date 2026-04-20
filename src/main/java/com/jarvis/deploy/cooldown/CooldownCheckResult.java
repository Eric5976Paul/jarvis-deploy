package com.jarvis.deploy.cooldown;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

/**
 * Represents the outcome of a cooldown check for a deployment.
 */
public class CooldownCheckResult {

    private final boolean allowed;
    private final String environment;
    private final String application;
    private final Duration remainingCooldown;
    private final Instant cooldownExpiresAt;
    private final String message;

    private CooldownCheckResult(boolean allowed, String environment, String application,
                                Duration remainingCooldown, Instant cooldownExpiresAt, String message) {
        this.allowed = allowed;
        this.environment = environment;
        this.application = application;
        this.remainingCooldown = remainingCooldown;
        this.cooldownExpiresAt = cooldownExpiresAt;
        this.message = message;
    }

    public static CooldownCheckResult allowed(String environment, String application) {
        return new CooldownCheckResult(true, environment, application, Duration.ZERO, null,
                "Deployment allowed: no active cooldown for " + application + " in " + environment);
    }

    public static CooldownCheckResult blocked(String environment, String application,
                                              Duration remaining, Instant expiresAt) {
        String msg = String.format(
                "Deployment blocked: cooldown active for %s in %s. Expires at %s (%.0f seconds remaining).",
                application, environment, expiresAt, (double) remaining.getSeconds());
        return new CooldownCheckResult(false, environment, application, remaining, expiresAt, msg);
    }

    public boolean isAllowed() {
        return allowed;
    }

    public boolean isBlocked() {
        return !allowed;
    }

    public String getEnvironment() {
        return environment;
    }

    public String getApplication() {
        return application;
    }

    public Duration getRemainingCooldown() {
        return remainingCooldown;
    }

    public Optional<Instant> getCooldownExpiresAt() {
        return Optional.ofNullable(cooldownExpiresAt);
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "CooldownCheckResult{allowed=" + allowed + ", message='" + message + "'}";
    }
}
