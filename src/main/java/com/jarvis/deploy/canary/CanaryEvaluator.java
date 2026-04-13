package com.jarvis.deploy.canary;

import java.util.logging.Logger;

/**
 * Evaluates canary deployment health based on configurable error rate and latency thresholds.
 */
public class CanaryEvaluator {

    private static final Logger log = Logger.getLogger(CanaryEvaluator.class.getName());

    private final double maxErrorRate;
    private final double maxP99LatencyMs;

    public CanaryEvaluator(double maxErrorRate, double maxP99LatencyMs) {
        this.maxErrorRate = maxErrorRate;
        this.maxP99LatencyMs = maxP99LatencyMs;
    }

    /**
     * Evaluates canary metrics and returns a promotion or rollback decision.
     *
     * @param config     the canary deployment configuration
     * @param errorRate  observed error rate (0.0 to 1.0)
     * @param p99Latency observed p99 latency in milliseconds
     * @return evaluation result with decision and reasoning
     */
    public CanaryEvaluationResult evaluate(CanaryDeploymentConfig config,
                                            double errorRate, double p99Latency) {
        log.info(String.format("Evaluating canary for %s v%s: errorRate=%.4f, p99=%.1fms",
                config.getAppName(), config.getCanaryVersion(), errorRate, p99Latency));

        if (errorRate < 0 || errorRate > 1) {
            return CanaryEvaluationResult.inconclusive("Invalid error rate value: " + errorRate);
        }

        if (errorRate > maxErrorRate) {
            String reason = String.format("Error rate %.2f%% exceeds threshold %.2f%%",
                    errorRate * 100, maxErrorRate * 100);
            log.warning("Canary rollback decision: " + reason);
            return CanaryEvaluationResult.rollback(reason, errorRate, p99Latency);
        }

        if (p99Latency > maxP99LatencyMs) {
            String reason = String.format("P99 latency %.1fms exceeds threshold %.1fms",
                    p99Latency, maxP99LatencyMs);
            log.warning("Canary rollback decision: " + reason);
            return CanaryEvaluationResult.rollback(reason, errorRate, p99Latency);
        }

        log.info("Canary promote decision for " + config.getAppName());
        return CanaryEvaluationResult.promote(errorRate, p99Latency);
    }

    public double getMaxErrorRate() { return maxErrorRate; }
    public double getMaxP99LatencyMs() { return maxP99LatencyMs; }
}
