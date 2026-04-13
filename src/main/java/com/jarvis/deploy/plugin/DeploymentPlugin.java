package com.jarvis.deploy.plugin;

/**
 * Interface for extending jarvis-deploy with custom deployment plugins.
 * Plugins can hook into lifecycle events during a deployment pipeline.
 */
public interface DeploymentPlugin {

    /**
     * Unique identifier for this plugin.
     */
    String getName();

    /**
     * Called before deployment begins for a given environment.
     *
     * @param context the current plugin execution context
     * @return a PluginResult indicating success or failure
     */
    PluginResult beforeDeploy(PluginContext context);

    /**
     * Called after deployment completes (regardless of outcome).
     *
     * @param context the current plugin execution context
     * @return a PluginResult indicating success or failure
     */
    PluginResult afterDeploy(PluginContext context);

    /**
     * Indicates whether this plugin is enabled.
     * Disabled plugins are skipped during pipeline execution.
     */
    default boolean isEnabled() {
        return true;
    }
}
