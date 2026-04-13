package com.jarvis.deploy.metrics;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Tracks deployment metrics such as success/failure counts and durations per environment.
 */
public class DeploymentMetrics {

    private final Map<String, AtomicInteger> successCounts = new ConcurrentHashMap<>();
    private final Map<String, AtomicInteger> failureCounts = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> totalDurationsMs = new ConcurrentHashMap<>();
    private final Map<String, AtomicInteger> deploymentCounts = new ConcurrentHashMap<>();

    public void recordSuccess(String environment, Duration duration) {
        if (environment == null || environment.isBlank()) {
            throw new IllegalArgumentException("Environment must not be null or blank");
        }
        successCounts.computeIfAbsent(environment, k -> new AtomicInteger(0)).incrementAndGet();
        accumulateDuration(environment, duration);
    }

    public void recordFailure(String environment, Duration duration) {
        if (environment == null || environment.isBlank()) {
            throw new IllegalArgumentException("Environment must not be null or blank");
        }
        failureCounts.computeIfAbsent(environment, k -> new AtomicInteger(0)).incrementAndGet();
        accumulateDuration(environment, duration);
    }

    private void accumulateDuration(String environment, Duration duration) {
        long ms = duration != null ? duration.toMillis() : 0L;
        totalDurationsMs.computeIfAbsent(environment, k -> new AtomicLong(0L)).addAndGet(ms);
        deploymentCounts.computeIfAbsent(environment, k -> new AtomicInteger(0)).incrementAndGet();
    }

    public int getSuccessCount(String environment) {
        return successCounts.getOrDefault(environment, new AtomicInteger(0)).get();
    }

    public int getFailureCount(String environment) {
        return failureCounts.getOrDefault(environment, new AtomicInteger(0)).get();
    }

    public double getAverageDurationMs(String environment) {
        int count = deploymentCounts.getOrDefault(environment, new AtomicInteger(0)).get();
        if (count == 0) return 0.0;
        long total = totalDurationsMs.getOrDefault(environment, new AtomicLong(0L)).get();
        return (double) total / count;
    }

    public MetricsSummary getSummary(String environment) {
        return new MetricsSummary(
            environment,
            getSuccessCount(environment),
            getFailureCount(environment),
            getAverageDurationMs(environment)
        );
    }

    public void reset(String environment) {
        successCounts.remove(environment);
        failureCounts.remove(environment);
        totalDurationsMs.remove(environment);
        deploymentCounts.remove(environment);
    }
}
