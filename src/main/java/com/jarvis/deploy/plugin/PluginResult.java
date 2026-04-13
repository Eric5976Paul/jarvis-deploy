package com.jarvis.deploy.plugin;

import java.util.Objects;

/**
 * Represents the outcome of a plugin lifecycle hook execution.
 */
public class PluginResult {

    private final boolean success;
    private final String message;
    private final boolean haltPipeline;

    private PluginResult(boolean success, String message, boolean haltPipeline) {
        this.success = success;
        this.message = message;
        this.haltPipeline = haltPipeline;
    }

    public static PluginResult ok() {
        return new PluginResult(true, "Plugin executed successfully.", false);
    }

    public static PluginResult ok(String message) {
        return new PluginResult(true, Objects.requireNonNull(message), false);
    }

    public static PluginResult failure(String message) {
        return new PluginResult(false, Objects.requireNonNull(message), false);
    }

    public static PluginResult fatalFailure(String message) {
        return new PluginResult(false, Objects.requireNonNull(message), true);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    /**
     * If true, the deployment pipeline should be halted immediately after this plugin.
     */
    public boolean shouldHaltPipeline() {
        return haltPipeline;
    }

    @Override
    public String toString() {
        return "PluginResult{" +
                "success=" + success +
                ", message='" + message + '\'' +
                ", haltPipeline=" + haltPipeline +
                '}';
    }
}
