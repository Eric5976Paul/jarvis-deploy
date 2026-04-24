package com.jarvis.deploy.compliancy;

import java.util.Objects;

/**
 * Represents a single compliance rule that must be satisfied before a deployment proceeds.
 */
public class ComplianceRule {

    private final String ruleId;
    private final String description;
    private final ComplianceSeverity severity;
    private final String environment; // null means applies to all environments

    public ComplianceRule(String ruleId, String description, ComplianceSeverity severity, String environment) {
        Objects.requireNonNull(ruleId, "ruleId must not be null");
        Objects.requireNonNull(description, "description must not be null");
        Objects.requireNonNull(severity, "severity must not be null");
        this.ruleId = ruleId;
        this.description = description;
        this.severity = severity;
        this.environment = environment;
    }

    public String getRuleId() {
        return ruleId;
    }

    public String getDescription() {
        return description;
    }

    public ComplianceSeverity getSeverity() {
        return severity;
    }

    public String getEnvironment() {
        return environment;
    }

    public boolean appliesToEnvironment(String env) {
        return environment == null || environment.equalsIgnoreCase(env);
    }

    @Override
    public String toString() {
        return "ComplianceRule{ruleId='" + ruleId + "', severity=" + severity +
                ", environment='" + (environment != null ? environment : "ALL") + "'}";
    }

    public enum ComplianceSeverity {
        INFO, WARNING, BLOCKING
    }
}
