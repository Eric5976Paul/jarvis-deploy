package com.jarvis.deploy.canary;

import java.time.Instant;

/**
 * Holds the outcome of a canary evaluation, indicating whether to promote or rollback.
 */
public class CanaryEvaluationResult {

    public enum Decision { PROMOTE, ROLLBACK, INCONCLUSIVE }

    private final Decision decision;
    private final String reason;
    private final double errorRate;
    private final double p99LatencyMs;
    private final Instant evaluatedAt;

    private CanaryEvaluationResult(Decision decision, String reason,
                                    double errorRate, double p99LatencyMs) {
        this.decision = decision;
        this.reason = reason;
        this.errorRate = errorRate;
        this.p99LatencyMs = p99LatencyMs;
        this.evaluatedAt = Instant.now();
    }

    public static CanaryEvaluationResult promote(double errorRate, double p99LatencyMs) {
        return new CanaryEvaluationResult(Decision.PROMOTE,
                "Canary metrics within acceptable thresholds", errorRate, p99LatencyMs);
    }

    public static CanaryEvaluationResult rollback(String reason, double errorRate, double p99LatencyMs) {
        return new CanaryEvaluationResult(Decision.ROLLBACK, reason, errorRate, p99LatencyMs);
    }

    public static CanaryEvaluationResult inconclusive(String reason) {
        return new CanaryEvaluationResult(Decision.INCONCLUSIVE, reason, 0.0, 0.0);
    }

    public Decision getDecision() { return decision; }
    public String getReason() { return reason; }
    public double getErrorRate() { return errorRate; }
    public double getP99LatencyMs() { return p99LatencyMs; }
    public Instant getEvaluatedAt() { return evaluatedAt; }
    public boolean isPromote() { return decision == Decision.PROMOTE; }
    public boolean isRollback() { return decision == Decision.ROLLBACK; }

    @Override
    public String toString() {
        return String.format("CanaryEvaluationResult{decision=%s, errorRate=%.2f%%, p99=%.1fms, reason='%s'}",
                decision, errorRate * 100, p99LatencyMs, reason);
    }
}
