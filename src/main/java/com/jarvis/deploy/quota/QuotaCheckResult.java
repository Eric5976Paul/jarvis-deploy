package com.jarvis.deploy.quota;

import java.time.Instant;
import java.util.Optional;

/**
 * Encapsulates the result of a deployment quota check.
 */
public class QuotaCheckResult {

    private final boolean allowed;
    private final String environment;
    private final int currentCount;
    private final int maxAllowed;
    private final String reason;
    private final Instant checkedAt;
    private final Instant resetAt;

    private QuotaCheckResult(Builder builder) {
        this.allowed = builder.allowed;
        this.environment = builder.environment;
        this.currentCount = builder.currentCount;
        this.maxAllowed = builder.maxAllowed;
        this.reason = builder.reason;
        this.checkedAt = builder.checkedAt != null ? builder.checkedAt : Instant.now();
        this.resetAt = builder.resetAt;
    }

    public boolean isAllowed() {
        return allowed;
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

    public Optional<String> getReason() {
        return Optional.ofNullable(reason);
    }

    public Instant getCheckedAt() {
        return checkedAt;
    }

    public Optional<Instant> getResetAt() {
        return Optional.ofNullable(resetAt);
    }

    public int getRemainingQuota() {
        return Math.max(0, maxAllowed - currentCount);
    }

    @Override
    public String toString() {
        return String.format("QuotaCheckResult{allowed=%b, env='%s', current=%d, max=%d, remaining=%d}",
                allowed, environment, currentCount, maxAllowed, getRemainingQuota());
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private boolean allowed;
        private String environment;
        private int currentCount;
        private int maxAllowed;
        private String reason;
        private Instant checkedAt;
        private Instant resetAt;

        public Builder allowed(boolean allowed) { this.allowed = allowed; return this; }
        public Builder environment(String environment) { this.environment = environment; return this; }
        public Builder currentCount(int currentCount) { this.currentCount = currentCount; return this; }
        public Builder maxAllowed(int maxAllowed) { this.maxAllowed = maxAllowed; return this; }
        public Builder reason(String reason) { this.reason = reason; return this; }
        public Builder checkedAt(Instant checkedAt) { this.checkedAt = checkedAt; return this; }
        public Builder resetAt(Instant resetAt) { this.resetAt = resetAt; return this; }
        public QuotaCheckResult build() { return new QuotaCheckResult(this); }
    }
}
