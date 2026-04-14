package com.jarvis.deploy.dependency;

/**
 * Defines the nature of a deployment dependency.
 */
public enum DependencyType {

    /**
     * The required service must be deployed and healthy before this deployment can proceed.
     */
    HARD,

    /**
     * The required service is preferred but deployment can proceed without it (with a warning).
     */
    SOFT,

    /**
     * The required service must be at least at the specified version (semver-aware).
     */
    VERSION_CONSTRAINT
}
