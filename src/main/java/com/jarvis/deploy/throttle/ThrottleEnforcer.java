package com.jarvis.deploy.throttle;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ThrottleEnforcer {

    private final Map<String, List<Instant>> deploymentTimestamps = new ConcurrentHashMap<>();
    private final Map<String, Instant> windowStarts = new ConcurrentHashMap<>();

    public ThrottleCheckResult check(DeploymentThrottle throttle) {
        String env = throttle.getEnvironment();
        Instant now = Instant.now();

        windowStarts.putIfAbsent(env, now);
        Instant windowStart = windowStarts.get(env);

        if (throttle.isWindowExpired(windowStart)) {
            windowStarts.put(env, now);
            deploymentTimestamps.put(env, new ArrayList<>());
            windowStart = now;
        }

        List<Instant> timestamps = deploymentTimestamps.computeIfAbsent(env, k -> new ArrayList<>());
        int count = timestamps.size();

        if (count >= throttle.getMaxDeploymentsPerWindow()) {
            return ThrottleCheckResult.exceeded(env, count, throttle.getMaxDeploymentsPerWindow(),
                    throttle.getActionOnExceed(), windowStart.plus(throttle.getWindowDuration()));
        }

        timestamps.add(now);
        return ThrottleCheckResult.allowed(env, count + 1, throttle.getMaxDeploymentsPerWindow());
    }

    public void reset(String environment) {
        deploymentTimestamps.remove(environment);
        windowStarts.remove(environment);
    }

    public int getCurrentCount(String environment) {
        List<Instant> ts = deploymentTimestamps.get(environment);
        return ts == null ? 0 : ts.size();
    }
}
