package com.jarvis.deploy.throttle;

import java.time.Instant;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Enforces deployment throttle policies per environment.
 */
public class ThrottleEnforcer {

    private final Map<String, Deque<Instant>> deploymentTimestamps = new ConcurrentHashMap<>();

    public ThrottleCheckResult check(String environment, DeploymentThrottle throttle) {
        if (throttle == null || throttle.getAction() == ThrottleAction.ALLOW) {
            return ThrottleCheckResult.allowed();
        }

        Deque<Instant> timestamps = deploymentTimestamps
                .computeIfAbsent(environment, k -> new LinkedList<>());

        Instant now = Instant.now();
        Instant windowStart = now.minusSeconds(throttle.getWindowSeconds());

        synchronized (timestamps) {
            // Remove entries outside the window
            while (!timestamps.isEmpty() && timestamps.peekFirst().isBefore(windowStart)) {
                timestamps.pollFirst();
            }

            int current = timestamps.size();
            if (current >= throttle.getMaxDeployments()) {
                String reason = String.format(
                        "Throttle limit reached: %d deployments in %ds window for env '%s'",
                        current, throttle.getWindowSeconds(), environment);
                return ThrottleCheckResult.throttled(reason, throttle.getAction());
            }

            timestamps.addLast(now);
            return ThrottleCheckResult.allowed();
        }
    }

    public void reset(String environment) {
        deploymentTimestamps.remove(environment);
    }

    public int currentCount(String environment, DeploymentThrottle throttle) {
        Deque<Instant> timestamps = deploymentTimestamps.get(environment);
        if (timestamps == null) return 0;
        Instant windowStart = Instant.now().minusSeconds(throttle.getWindowSeconds());
        synchronized (timestamps) {
            return (int) timestamps.stream().filter(t -> t.isAfter(windowStart)).count();
        }
    }
}
