package com.jarvis.deploy.gate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages registration and evaluation of deployment gates per environment.
 * Gates act as quality/compliance checkpoints before a deployment is allowed to proceed.
 */
public class DeploymentGateService {

    private final Map<String, List<DeploymentGate>> gatesByEnvironment = new ConcurrentHashMap<>();

    /**
     * Registers a gate for the given environment.
     */
    public void registerGate(DeploymentGate gate) {
        Objects.requireNonNull(gate, "gate must not be null");
        gatesByEnvironment
            .computeIfAbsent(gate.getEnvironment(), k -> new ArrayList<>())
            .add(gate);
    }

    /**
     * Returns all gates registered for the given environment.
     */
    public List<DeploymentGate> getGatesForEnvironment(String environment) {
        return List.copyOf(gatesByEnvironment.getOrDefault(environment, List.of()));
    }

    /**
     * Evaluates all gates for the specified environment.
     * Each gate is passed or failed based on the provided evaluator function context.
     * For this implementation, gates are evaluated by checking their current status;
     * callers should set gate status before invoking this method.
     *
     * @param environment the target environment
     * @param evaluatedBy identifier of the actor triggering evaluation
     * @return a GateEvaluationResult summarising the outcome
     */
    public GateEvaluationResult evaluateGates(String environment, String evaluatedBy) {
        List<DeploymentGate> gates = getGatesForEnvironment(environment);
        if (gates.isEmpty()) {
            return GateEvaluationResult.success(List.of());
        }

        List<String> failures = new ArrayList<>();
        for (DeploymentGate gate : gates) {
            if (gate.isPending()) {
                // Auto-pass pending gates with no explicit override (default permissive)
                gate.pass(evaluatedBy);
            }
            if (gate.isFailed()) {
                failures.add("Gate '" + gate.getName() + "' (" + gate.getGateId() + ") FAILED");
            }
        }

        if (failures.isEmpty()) {
            return GateEvaluationResult.success(new ArrayList<>(gates));
        }
        return GateEvaluationResult.failure(new ArrayList<>(gates), failures);
    }

    /**
     * Removes all gates for the given environment.
     */
    public void clearGates(String environment) {
        gatesByEnvironment.remove(environment);
    }

    /**
     * Returns total number of registered gates across all environments.
     */
    public int totalGateCount() {
        return gatesByEnvironment.values().stream().mapToInt(List::size).sum();
    }
}
