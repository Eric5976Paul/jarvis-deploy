package com.jarvis.deploy.stealth;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages stealth mode lifecycle for deployments.
 * Stealth mode suppresses notifications and/or audit events
 * for a bounded time window during sensitive operations.
 */
public class StealthModeService {

    private static final Duration MAX_STEALTH_DURATION = Duration.ofHours(4);

    private final Map<String, StealthMode> activeModes = new ConcurrentHashMap<>();

    public StealthModeResult activate(String deploymentId, String environment,
                                      boolean suppressNotifications, boolean suppressAudit,
                                      String reason, Duration duration) {
        if (deploymentId == null || deploymentId.isBlank()) {
            return StealthModeResult.rejected("deploymentId must not be blank");
        }
        if (duration != null && duration.compareTo(MAX_STEALTH_DURATION) > 0) {
            return StealthModeResult.rejected(
                "Requested stealth duration exceeds maximum of " + MAX_STEALTH_DURATION);
        }

        StealthMode existing = activeModes.get(deploymentId);
        if (existing != null && existing.isActive(Instant.now())) {
            return StealthModeResult.alreadyActive(existing);
        }

        Instant now = Instant.now();
        Instant expiresAt = duration != null ? now.plus(duration) : null;
        StealthMode mode = new StealthMode(
            deploymentId, environment,
            suppressNotifications, suppressAudit,
            reason, now, expiresAt
        );
        activeModes.put(deploymentId, mode);
        return StealthModeResult.activated(mode);
    }

    public StealthModeResult deactivate(String deploymentId) {
        StealthMode removed = activeModes.remove(deploymentId);
        if (removed == null) {
            return StealthModeResult.notFound(deploymentId);
        }
        return StealthModeResult.deactivated(deploymentId);
    }

    public Optional<StealthMode> getActive(String deploymentId) {
        StealthMode mode = activeModes.get(deploymentId);
        if (mode == null) return Optional.empty();
        if (mode.isExpired(Instant.now())) {
            activeModes.remove(deploymentId);
            return Optional.empty();
        }
        return Optional.of(mode);
    }

    public boolean isStealthActive(String deploymentId) {
        return getActive(deploymentId).isPresent();
    }

    public boolean shouldSuppressNotifications(String deploymentId) {
        return getActive(deploymentId)
            .map(StealthMode::isSuppressNotifications)
            .orElse(false);
    }

    public boolean shouldSuppressAudit(String deploymentId) {
        return getActive(deploymentId)
            .map(StealthMode::isSuppressAudit)
            .orElse(false);
    }

    public void evictExpired() {
        Instant now = Instant.now();
        activeModes.entrySet().removeIf(e -> e.getValue().isExpired(now));
    }
}
