package com.jarvis.deploy.ratelimit;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Enforces per-environment deployment rate limits using a sliding-window counter.
 */
public class DeploymentRateLimiter {

    private final int maxDeployments;
    private final long windowSeconds;
    private final Map<String, Deque<Instant>> windowMap = new ConcurrentHashMap<>();

    public DeploymentRateLimiter(int maxDeployments, long windowSeconds) {
        if (maxDeployments <= 0) throw new IllegalArgumentException("maxDeployments must be positive");
        if (windowSeconds <= 0) throw new IllegalArgumentException("windowSeconds must be positive");
        this.maxDeployments = maxDeployments;
        this.windowSeconds = windowSeconds;
    }

    /**
     * Attempts to acquire a deployment slot for the given environment.
     *
     * @param environment the target environment identifier
     * @return a {@link RateLimitResult} indicating whether the request is allowed
     */
    public synchronized RateLimitResult tryAcquire(String environment) {
        if (environment == null || environment.isBlank()) {
            return RateLimitResult.denied(environment, 0, maxDeployments, "Environment must not be blank");
        }

        Instant now = Instant.now();
        Instant windowStart = now.minusSeconds(windowSeconds);

        Deque<Instant> timestamps = windowMap.computeIfAbsent(environment, k -> new ArrayDeque<>());

        // Evict entries outside the sliding window
        while (!timestamps.isEmpty() && timestamps.peekFirst().isBefore(windowStart)) {
            timestamps.pollFirst();
        }

        int current = timestamps.size();
        if (current >= maxDeployments) {
            long retryAfter = windowSeconds - (now.getEpochSecond() - timestamps.peekFirst().getEpochSecond());
            return RateLimitResult.denied(environment, current, maxDeployments,
                    "Rate limit exceeded. Retry after " + retryAfter + "s");
        }

        timestamps.addLast(now);
        return RateLimitResult.allowed(environment, current + 1, maxDeployments);
    }

    /**
     * Resets the rate limit counters for a specific environment.
     */
    public synchronized void reset(String environment) {
        windowMap.remove(environment);
    }

    /**
     * Returns the current deployment count within the window for an environment.
     */
    public synchronized int currentCount(String environment) {
        Deque<Instant> timestamps = windowMap.get(environment);
        if (timestamps == null) return 0;
        Instant windowStart = Instant.now().minusSeconds(windowSeconds);
        timestamps.removeIf(t -> t.isBefore(windowStart));
        return timestamps.size();
    }
}
