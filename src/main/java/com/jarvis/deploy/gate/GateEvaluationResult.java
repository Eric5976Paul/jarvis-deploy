package com.jarvis.deploy.gate;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Holds the outcome of evaluating all gates for a deployment.
 */
public class GateEvaluationResult {

    private final boolean allPassed;
    private final List<DeploymentGate> gates;
    private final List<String> failureReasons;

    private GateEvaluationResult(boolean allPassed, List<DeploymentGate> gates, List<String> failureReasons) {
        this.allPassed = allPassed;
        this.gates = Collections.unmodifiableList(gates);
        this.failureReasons = Collections.unmodifiableList(failureReasons);
    }

    public static GateEvaluationResult success(List<DeploymentGate> gates) {
        return new GateEvaluationResult(true, gates, List.of());
    }

    public static GateEvaluationResult failure(List<DeploymentGate> gates, List<String> reasons) {
        Objects.requireNonNull(reasons, "reasons must not be null");
        return new GateEvaluationResult(false, gates, reasons);
    }

    public boolean isAllPassed() { return allPassed; }
    public List<DeploymentGate> getGates() { return gates; }
    public List<String> getFailureReasons() { return failureReasons; }

    public long countPassed() {
        return gates.stream().filter(DeploymentGate::isPassed).count();
    }

    public long countFailed() {
        return gates.stream().filter(DeploymentGate::isFailed).count();
    }

    @Override
    public String toString() {
        return "GateEvaluationResult{allPassed=" + allPassed +
               ", passed=" + countPassed() +
               ", failed=" + countFailed() +
               ", reasons=" + failureReasons + "}";
    }
}
