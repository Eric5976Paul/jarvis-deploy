package com.jarvis.deploy.variable;

/**
 * Defines the scope at which a deployment variable applies.
 */
public enum VariableScope {

    /** Applies globally across all environments. */
    GLOBAL,

    /** Applies to a specific environment (e.g., staging, production). */
    ENVIRONMENT,

    /** Applies only to a specific deployment run. */
    DEPLOYMENT;

    /**
     * Returns true if this scope is more specific than the given scope.
     * Specificity order: DEPLOYMENT > ENVIRONMENT > GLOBAL
     */
    public boolean isMoreSpecificThan(VariableScope other) {
        return this.ordinal() > other.ordinal();
    }
}
