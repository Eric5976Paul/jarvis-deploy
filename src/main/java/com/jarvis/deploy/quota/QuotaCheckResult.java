package com.jarvis.deploy.quota;

/**
 * Represents the outcome of a deployment quota check.
 */
public class QuotaCheckResult {

    private final boolean allowed;
    private final String environment;
    private final int currentCount;
    private final int maxAllowed;
    private final String message;

    private QuotaCheckResult(boolean allowed, String environment, int currentCount, int maxAllowed, String message) {
        this.allowed = allowed;
        this.environment = environment;
        this.currentCount = currentCount;
        this.maxAllowed = maxAllowed;
        this.message = message;
    }

    public static QuotaCheckResult allowed(String environment, int currentCount, int maxAllowed) {
        String msg = String.format("Deployment allowed for '%s': %d/%d used", environment, currentCount, maxAllowed);
        return new QuotaCheckResult(true, environment, currentCount, maxAllowed, msg);
    }

    public static QuotaCheckResult denied(String environment, int currentCount, int maxAllowed) {
        String msg = String.format("Quota exceeded for '%s': %d/%d deployments in current window",
                environment, currentCount, maxAllowed);
        return new QuotaCheckResult(false, environment, currentCount, maxAllowed, msg);
    }

    public boolean isAllowed() {
        return allowed;
    }

    public boolean isDenied() {
        return !allowed;
    }

    public String getEnvironment() {
        return environment;
    }

    public int getCurrentCount() {
        return currentCount;
    }

    public int getMaxAllowed() {
        return maxAllowed;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return message;
    }
}
