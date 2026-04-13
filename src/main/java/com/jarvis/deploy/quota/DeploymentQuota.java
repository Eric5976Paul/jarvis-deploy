package com.jarvis.deploy.quota;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Tracks and enforces deployment quota limits per environment.
 */
public class DeploymentQuota {

    private final String environment;
    private final int maxDeploymentsPerHour;
    private final AtomicInteger deploymentCount;
    private Instant windowStart;

    public DeploymentQuota(String environment, int maxDeploymentsPerHour) {
        if (environment == null || environment.isBlank()) {
            throw new IllegalArgumentException("Environment must not be blank");
        }
        if (maxDeploymentsPerHour <= 0) {
            throw new IllegalArgumentException("Max deployments per hour must be positive");
        }
        this.environment = environment;
        this.maxDeploymentsPerHour = maxDeploymentsPerHour;
        this.deploymentCount = new AtomicInteger(0);
        this.windowStart = Instant.now();
    }

    /**
     * Attempts to consume one deployment slot.
     *
     * @return QuotaCheckResult indicating whether the quota allows the deployment
     */
    public QuotaCheckResult tryConsume() {
        resetWindowIfExpired();
        int current = deploymentCount.incrementAndGet();
        if (current > maxDeploymentsPerHour) {
            deploymentCount.decrementAndGet();
            return QuotaCheckResult.denied(environment, current - 1, maxDeploymentsPerHour);
        }
        return QuotaCheckResult.allowed(environment, current, maxDeploymentsPerHour);
    }

    /**
     * Returns current usage without consuming a slot.
     */
    public int currentUsage() {
        resetWindowIfExpired();
        return deploymentCount.get();
    }

    public String getEnvironment() {
        return environment;
    }

    public int getMaxDeploymentsPerHour() {
        return maxDeploymentsPerHour;
    }

    public Instant getWindowStart() {
        return windowStart;
    }

    private void resetWindowIfExpired() {
        Instant now = Instant.now();
        if (now.isAfter(windowStart.plusSeconds(3600))) {
            deploymentCount.set(0);
            windowStart = now;
        }
    }
}
