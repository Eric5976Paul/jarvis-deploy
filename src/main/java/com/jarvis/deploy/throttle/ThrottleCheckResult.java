package com.jarvis.deploy.throttle;

import java.time.Instant;

public class ThrottleCheckResult {

    private final boolean allowed;
    private final String environment;
    private final int currentCount;
    private final int limit;
    private final ThrottleAction action;
    private final Instant windowResetAt;
    private final String message;

    private ThrottleCheckResult(boolean allowed, String environment, int currentCount, int limit,
                                 ThrottleAction action, Instant windowResetAt, String message) {
        this.allowed = allowed;
        this.environment = environment;
        this.currentCount = currentCount;
        this.limit = limit;
        this.action = action;
        this.windowResetAt = windowResetAt;
        this.message = message;
    }

    public static ThrottleCheckResult allowed(String env, int count, int limit) {
        return new ThrottleCheckResult(true, env, count, limit, null, null,
                "Deployment allowed [" + count + "/" + limit + "]");
    }

    public static ThrottleCheckResult exceeded(String env, int count, int limit,
                                                ThrottleAction action, Instant resetAt) {
        return new ThrottleCheckResult(false, env, count, limit, action, resetAt,
                "Throttle exceeded for '" + env + "': " + count + "/" + limit + " — action: " + action);
    }

    public boolean isAllowed() { return allowed; }
    public String getEnvironment() { return environment; }
    public int getCurrentCount() { return currentCount; }
    public int getLimit() { return limit; }
    public ThrottleAction getAction() { return action; }
    public Instant getWindowResetAt() { return windowResetAt; }
    public String getMessage() { return message; }

    @Override
    public String toString() { return message; }
}
