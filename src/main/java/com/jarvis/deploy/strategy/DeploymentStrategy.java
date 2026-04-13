package com.jarvis.deploy.strategy;

import com.jarvis.deploy.config.DeploymentConfig;

/**
 * Defines the contract for deployment execution strategies.
 * Implementations include rolling, blue-green, and canary strategies.
 */
public interface DeploymentStrategy {

    /**
     * Returns the unique name identifying this strategy.
     */
    String getName();

    /**
     * Executes the deployment using the given config.
     *
     * @param config the resolved deployment configuration
     * @return a StrategyResult describing the outcome
     */
    StrategyResult execute(DeploymentConfig config);

    /**
     * Validates that this strategy is applicable for the given config.
     *
     * @param config the deployment configuration to validate against
     * @return true if this strategy can be applied
     */
    boolean supports(DeploymentConfig config);

    /**
     * Provides a human-readable description of how this strategy operates.
     */
    default String describe() {
        return "Deployment strategy: " + getName();
    }
}
