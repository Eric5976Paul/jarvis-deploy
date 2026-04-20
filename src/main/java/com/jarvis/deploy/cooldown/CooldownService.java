package com.jarvis.deploy.cooldown;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages deployment cooldown periods per environment and application.
 * Prevents deployments from occurring too frequently.
 */
public class CooldownService {

    private final Duration defaultCooldown;
    private final Map<String, DeploymentCooldown> cooldownRegistry = new ConcurrentHashMap<>();

    public CooldownService(Duration defaultCooldown) {
        if (defaultCooldown == null || defaultCooldown.isNegative()) {
            throw new IllegalArgumentException("defaultCooldown must be a non-negative duration");
        }
        this.defaultCooldown = defaultCooldown;
    }

    /**
     * Records a deployment, starting a cooldown for the given environment and application.
     */
    public void recordDeployment(String environment, String application) {
        recordDeployment(environment, application, defaultCooldown);
    }

    /**
     * Records a deployment with a custom cooldown duration.
     */
    public void recordDeployment(String environment, String application, Duration cooldown) {
        String key = buildKey(environment, application);
        DeploymentCooldown entry = new DeploymentCooldown(environment, application, cooldown, Instant.now());
        cooldownRegistry.put(key, entry);
    }

    /**
     * Checks whether a deployment is currently allowed (cooldown has expired or no record exists).
     */
    public CooldownCheckResult checkCooldown(String environment, String application) {
        String key = buildKey(environment, application);
        DeploymentCooldown cooldown = cooldownRegistry.get(key);
        if (cooldown == null) {
            return CooldownCheckResult.allowed(environment, application);
        }
        Instant now = Instant.now();
        if (cooldown.isExpired(now)) {
            return CooldownCheckResult.allowed(environment, application);
        }
        Duration remaining = cooldown.remainingCooldown(now);
        return CooldownCheckResult.blocked(environment, application, remaining, cooldown.getCooldownExpiresAt());
    }

    public Optional<DeploymentCooldown> getCooldown(String environment, String application) {
        return Optional.ofNullable(cooldownRegistry.get(buildKey(environment, application)));
    }

    public void clearCooldown(String environment, String application) {
        cooldownRegistry.remove(buildKey(environment, application));
    }

    public int activeCooldownCount() {
        Instant now = Instant.now();
        return (int) cooldownRegistry.values().stream()
                .filter(c -> !c.isExpired(now))
                .count();
    }

    private String buildKey(String environment, String application) {
        return environment + ":" + application;
    }
}
