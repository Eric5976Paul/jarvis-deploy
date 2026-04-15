package com.jarvis.deploy.gate;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Represents a named gate that must be passed before a deployment proceeds.
 * A gate can have multiple conditions, all of which must be satisfied.
 */
public class DeploymentGate {

    private final String gateId;
    private final String name;
    private final String environment;
    private final List<String> conditions;
    private GateStatus status;
    private Instant evaluatedAt;
    private String evaluatedBy;

    public DeploymentGate(String gateId, String name, String environment, List<String> conditions) {
        this.gateId = Objects.requireNonNull(gateId, "gateId must not be null");
        this.name = Objects.requireNonNull(name, "name must not be null");
        this.environment = Objects.requireNonNull(environment, "environment must not be null");
        this.conditions = Collections.unmodifiableList(new ArrayList<>(Objects.requireNonNull(conditions)));
        this.status = GateStatus.PENDING;
    }

    public String getGateId() { return gateId; }
    public String getName() { return name; }
    public String getEnvironment() { return environment; }
    public List<String> getConditions() { return conditions; }
    public GateStatus getStatus() { return status; }
    public Instant getEvaluatedAt() { return evaluatedAt; }
    public String getEvaluatedBy() { return evaluatedBy; }

    public void pass(String evaluatedBy) {
        this.status = GateStatus.PASSED;
        this.evaluatedAt = Instant.now();
        this.evaluatedBy = evaluatedBy;
    }

    public void fail(String evaluatedBy) {
        this.status = GateStatus.FAILED;
        this.evaluatedAt = Instant.now();
        this.evaluatedBy = evaluatedBy;
    }

    public boolean isPassed() { return status == GateStatus.PASSED; }
    public boolean isFailed() { return status == GateStatus.FAILED; }
    public boolean isPending() { return status == GateStatus.PENDING; }

    @Override
    public String toString() {
        return "DeploymentGate{gateId='" + gateId + "', name='" + name +
               "', environment='" + environment + "', status=" + status + "}";
    }
}
