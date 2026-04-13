package com.jarvis.deploy.strategy;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Captures the outcome of a deployment strategy execution.
 */
public class StrategyResult {

    public enum Status { SUCCESS, PARTIAL, FAILED }

    private final Status status;
    private final String strategyName;
    private final String message;
    private final List<String> steps;
    private final Instant completedAt;

    public StrategyResult(Status status, String strategyName, String message, List<String> steps) {
        this.status = Objects.requireNonNull(status, "status must not be null");
        this.strategyName = Objects.requireNonNull(strategyName, "strategyName must not be null");
        this.message = message != null ? message : "";
        this.steps = steps != null ? Collections.unmodifiableList(steps) : Collections.emptyList();
        this.completedAt = Instant.now();
    }

    public static StrategyResult success(String strategyName, String message, List<String> steps) {
        return new StrategyResult(Status.SUCCESS, strategyName, message, steps);
    }

    public static StrategyResult failed(String strategyName, String message, List<String> steps) {
        return new StrategyResult(Status.FAILED, strategyName, message, steps);
    }

    public static StrategyResult partial(String strategyName, String message, List<String> steps) {
        return new StrategyResult(Status.PARTIAL, strategyName, message, steps);
    }

    public boolean isSuccessful() {
        return status == Status.SUCCESS;
    }

    public Status getStatus() { return status; }
    public String getStrategyName() { return strategyName; }
    public String getMessage() { return message; }
    public List<String> getSteps() { return steps; }
    public Instant getCompletedAt() { return completedAt; }

    @Override
    public String toString() {
        return "StrategyResult{" +
                "status=" + status +
                ", strategy='" + strategyName + '\'' +
                ", message='" + message + '\'' +
                ", steps=" + steps.size() +
                ", completedAt=" + completedAt +
                '}';
    }
}
