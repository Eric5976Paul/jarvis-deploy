package com.jarvis.deploy.summary;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Represents a summarized report of deployment activity for a given environment
 * over a specified time window.
 */
public class DeploymentSummaryReport {

    private final String environment;
    private final Instant from;
    private final Instant to;
    private final int totalDeployments;
    private final int successfulDeployments;
    private final int failedDeployments;
    private final int rolledBackDeployments;
    private final long averageDurationMs;
    private final Map<String, Integer> deploymentsByService;
    private final List<String> topFailureReasons;

    private DeploymentSummaryReport(Builder builder) {
        this.environment = Objects.requireNonNull(builder.environment, "environment must not be null");
        this.from = Objects.requireNonNull(builder.from, "from must not be null");
        this.to = Objects.requireNonNull(builder.to, "to must not be null");
        this.totalDeployments = builder.totalDeployments;
        this.successfulDeployments = builder.successfulDeployments;
        this.failedDeployments = builder.failedDeployments;
        this.rolledBackDeployments = builder.rolledBackDeployments;
        this.averageDurationMs = builder.averageDurationMs;
        this.deploymentsByService = Collections.unmodifiableMap(builder.deploymentsByService);
        this.topFailureReasons = Collections.unmodifiableList(builder.topFailureReasons);
    }

    public String getEnvironment() { return environment; }
    public Instant getFrom() { return from; }
    public Instant getTo() { return to; }
    public int getTotalDeployments() { return totalDeployments; }
    public int getSuccessfulDeployments() { return successfulDeployments; }
    public int getFailedDeployments() { return failedDeployments; }
    public int getRolledBackDeployments() { return rolledBackDeployments; }
    public long getAverageDurationMs() { return averageDurationMs; }
    public Map<String, Integer> getDeploymentsByService() { return deploymentsByService; }
    public List<String> getTopFailureReasons() { return topFailureReasons; }

    public double getSuccessRate() {
        if (totalDeployments == 0) return 0.0;
        return (double) successfulDeployments / totalDeployments * 100.0;
    }

    public static Builder builder(String environment, Instant from, Instant to) {
        return new Builder(environment, from, to);
    }

    public static class Builder {
        private final String environment;
        private final Instant from;
        private final Instant to;
        private int totalDeployments;
        private int successfulDeployments;
        private int failedDeployments;
        private int rolledBackDeployments;
        private long averageDurationMs;
        private Map<String, Integer> deploymentsByService = Collections.emptyMap();
        private List<String> topFailureReasons = Collections.emptyList();

        private Builder(String environment, Instant from, Instant to) {
            this.environment = environment;
            this.from = from;
            this.to = to;
        }

        public Builder totalDeployments(int val) { this.totalDeployments = val; return this; }
        public Builder successfulDeployments(int val) { this.successfulDeployments = val; return this; }
        public Builder failedDeployments(int val) { this.failedDeployments = val; return this; }
        public Builder rolledBackDeployments(int val) { this.rolledBackDeployments = val; return this; }
        public Builder averageDurationMs(long val) { this.averageDurationMs = val; return this; }
        public Builder deploymentsByService(Map<String, Integer> val) { this.deploymentsByService = val; return this; }
        public Builder topFailureReasons(List<String> val) { this.topFailureReasons = val; return this; }

        public DeploymentSummaryReport build() { return new DeploymentSummaryReport(this); }
    }
}
