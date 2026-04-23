package com.jarvis.deploy.ratelimit;

import java.util.Objects;

/**
 * Represents the outcome of a rate-limit acquisition attempt.
 */
public final class RateLimitResult {

    private final String environment;
    private final boolean allowed;
    private final int currentCount;
    private final int maxAllowed;
    private final String reason;

    private RateLimitResult(String environment, boolean allowed, int currentCount, int maxAllowed, String reason) {
        this.environment = environment;
        this.allowed = allowed;
        this.currentCount = currentCount;
        this.maxAllowed = maxAllowed;
        this.reason = reason;
    }

    public static RateLimitResult allowed(String environment, int currentCount, int maxAllowed) {
        return new RateLimitResult(environment, true, currentCount, maxAllowed, null);
    }

    public static RateLimitResult denied(String environment, int currentCount, int maxAllowed, String reason) {
        Objects.requireNonNull(reason, "Denial reason must not be null");
        return new RateLimitResult(environment, false, currentCount, maxAllowed, reason);
    }

    public String getEnvironment() { return environment; }
    public boolean isAllowed() { return allowed; }
    public int getCurrentCount() { return currentCount; }
    public int getMaxAllowed() { return maxAllowed; }
    public String getReason() { return reason; }

    public int getRemainingSlots() {
        return Math.max(0, maxAllowed - currentCount);
    }

    @Override
    public String toString() {
        if (allowed) {
            return String.format("RateLimitResult[ALLOWED env=%s count=%d/%d]", environment, currentCount, maxAllowed);
        }
        return String.format("RateLimitResult[DENIED env=%s count=%d/%d reason='%s']",
                environment, currentCount, maxAllowed, reason);
    }
}
