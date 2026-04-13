package com.jarvis.deploy.strategy;

import com.jarvis.deploy.deployment.DeploymentRecord;

import java.time.Instant;
import java.util.Objects;

/**
 * Executes a deployment strategy for a given record.
 * Handles retry delegation and result wrapping.
 */
public class StrategyExecutor {

    private final DeploymentStrategyRegistry registry;

    public StrategyExecutor(DeploymentStrategyRegistry registry) {
        this.registry = Objects.requireNonNull(registry, "Registry must not be null");
    }

    public StrategyResult execute(String strategyName, DeploymentRecord record) {
        if (record == null) {
            return StrategyResult.failure(strategyName, "DeploymentRecord must not be null");
        }

        DeploymentStrategy strategy = registry.find(strategyName).orElse(null);
        if (strategy == null) {
            return StrategyResult.failure(
                strategyName,
                "No strategy registered with name: '" + strategyName + "'"
            );
        }

        try {
            return strategy.execute(record);
        } catch (Exception ex) {
            return StrategyResult.failure(
                strategyName,
                "Strategy execution threw exception: " + ex.getMessage()
            );
        }
    }

    public boolean supports(String strategyName) {
        return registry.isRegistered(strategyName);
    }
}
