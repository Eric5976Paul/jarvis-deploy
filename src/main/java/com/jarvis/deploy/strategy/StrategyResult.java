package com.jarvis.deploy.strategy;

import java.time.Instant;
import java.util.Objects;

/**
 * Represents the outcome of executing a deployment strategy.
 */
public class StrategyResult {

    private final String strategyName;
    private final boolean success;
    private final String message;
    private final Instant executedAt;

    private StrategyResult(String strategyName, boolean success, String message, Instant executedAt) {
        this.strategyName = strategyName;
        this.success = success;
        this.message = message;
        this.executedAt = executedAt;
    }

    public static StrategyResult success(String strategyName, String message) {
        return new StrategyResult(
            Objects.requireNonNull(strategyName),
            true,
            message,
            Instant.now()
        );
    }

    public static StrategyResult failure(String strategyName, String reason) {
        return new StrategyResult(
            strategyName != null ? strategyName : "unknown",
            false,
            reason,
            Instant.now()
        );
    }

    public String getStrategyName() { return strategyName; }
    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public Instant getExecutedAt() { return executedAt; }

    @Override
    public String toString() {
        return "StrategyResult{strategy='" + strategyName +
               "', success=" + success +
               ", message='" + message + "'}";
    }
}
