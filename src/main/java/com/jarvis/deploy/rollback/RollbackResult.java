package com.jarvis.deploy.rollback;

/**
 * Immutable value object representing the outcome of a rollback operation.
 */
public class RollbackResult {

    private final String environment;
    private final boolean successful;
    private final String version;
    private final String message;

    private RollbackResult(String environment, boolean successful, String version, String message) {
        this.environment = environment;
        this.successful = successful;
        this.version = version;
        this.message = message;
    }

    public static RollbackResult success(String environment, String version) {
        return new RollbackResult(
                environment,
                true,
                version,
                "Rollback to version " + version + " completed successfully"
        );
    }

    public static RollbackResult failure(String environment, String reason) {
        return new RollbackResult(environment, false, null, reason);
    }

    public String getEnvironment() {
        return environment;
    }

    public boolean isSuccessful() {
        return successful;
    }

    public String getVersion() {
        return version;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "RollbackResult{" +
                "environment='" + environment + '\'' +
                ", successful=" + successful +
                ", version='" + version + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}
