package com.jarvis.deploy.metrics;

/**
 * Immutable snapshot of deployment metrics for a given environment.
 */
public class MetricsSummary {

    private final String environment;
    private final int successCount;
    private final int failureCount;
    private final double averageDurationMs;

    public MetricsSummary(String environment, int successCount, int failureCount, double averageDurationMs) {
        this.environment = environment;
        this.successCount = successCount;
        this.failureCount = failureCount;
        this.averageDurationMs = averageDurationMs;
    }

    public String getEnvironment() {
        return environment;
    }

    public int getSuccessCount() {
        return successCount;
    }

    public int getFailureCount() {
        return failureCount;
    }

    public double getAverageDurationMs() {
        return averageDurationMs;
    }

    public int getTotalCount() {
        return successCount + failureCount;
    }

    public double getSuccessRate() {
        int total = getTotalCount();
        if (total == 0) return 0.0;
        return (double) successCount / total * 100.0;
    }

    @Override
    public String toString() {
        return String.format(
            "MetricsSummary{env='%s', success=%d, failure=%d, avgDurationMs=%.2f, successRate=%.1f%%}",
            environment, successCount, failureCount, averageDurationMs, getSuccessRate()
        );
    }
}
