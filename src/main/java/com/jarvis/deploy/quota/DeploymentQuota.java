package com.jarvis.deploy.quota;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Tracks and enforces deployment quotas per environment within a rolling time window.
 */
public class DeploymentQuota {

    private final int maxDeploymentsPerWindow;
    private final Duration windowDuration;
    private final Map<String, WindowCounter> counters = new ConcurrentHashMap<>();

    public DeploymentQuota(int maxDeploymentsPerWindow, Duration windowDuration) {
        if (maxDeploymentsPerWindow <= 0) {
            throw new IllegalArgumentException("maxDeploymentsPerWindow must be positive");
        }
        if (windowDuration == null || windowDuration.isNegative() || windowDuration.isZero()) {
            throw new IllegalArgumentException("windowDuration must be a positive duration");
        }
        this.maxDeploymentsPerWindow = maxDeploymentsPerWindow;
        this.windowDuration = windowDuration;
    }

    public QuotaCheckResult check(String environment) {
        WindowCounter counter = counters.computeIfAbsent(environment, e -> new WindowCounter());
        counter.evictExpired(windowDuration);
        int current = counter.count();
        boolean allowed = current < maxDeploymentsPerWindow;
        return QuotaCheckResult.builder()
                .environment(environment)
                .currentCount(current)
                .maxAllowed(maxDeploymentsPerWindow)
                .allowed(allowed)
                .reason(allowed ? null : "Quota of " + maxDeploymentsPerWindow + " deployments per " + windowDuration + " exceeded for environment '" + environment + "'")
                .resetAt(counter.oldestTimestamp() != null ? counter.oldestTimestamp().plus(windowDuration) : null)
                .build();
    }

    public QuotaCheckResult recordAndCheck(String environment) {
        WindowCounter counter = counters.computeIfAbsent(environment, e -> new WindowCounter());
        counter.evictExpired(windowDuration);
        int current = counter.count();
        boolean allowed = current < maxDeploymentsPerWindow;
        if (allowed) {
            counter.record();
            current++;
        }
        return QuotaCheckResult.builder()
                .environment(environment)
                .currentCount(current)
                .maxAllowed(maxDeploymentsPerWindow)
                .allowed(allowed)
                .reason(allowed ? null : "Quota exceeded for environment '" + environment + "'")
                .resetAt(counter.oldestTimestamp() != null ? counter.oldestTimestamp().plus(windowDuration) : null)
                .build();
    }

    public void reset(String environment) {
        counters.remove(environment);
    }

    private static class WindowCounter {
        private final java.util.Deque<Instant> timestamps = new java.util.ArrayDeque<>();

        synchronized void record() {
            timestamps.addLast(Instant.now());
        }

        synchronized void evictExpired(Duration window) {
            Instant cutoff = Instant.now().minus(window);
            while (!timestamps.isEmpty() && timestamps.peekFirst().isBefore(cutoff)) {
                timestamps.pollFirst();
            }
        }

        synchronized int count() {
            return timestamps.size();
        }

        synchronized Instant oldestTimestamp() {
            return timestamps.isEmpty() ? null : timestamps.peekFirst();
        }
    }
}
