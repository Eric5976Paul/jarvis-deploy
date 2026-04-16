package com.jarvis.deploy.throttle;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

public class DeploymentThrottle {

    private final String environment;
    private final int maxDeploymentsPerWindow;
    private final Duration windowDuration;
    private final ThrottleAction actionOnExceed;

    public DeploymentThrottle(String environment, int maxDeploymentsPerWindow,
                               Duration windowDuration, ThrottleAction actionOnExceed) {
        if (maxDeploymentsPerWindow <= 0) throw new IllegalArgumentException("maxDeploymentsPerWindow must be positive");
        Objects.requireNonNull(windowDuration, "windowDuration must not be null");
        this.environment = Objects.requireNonNull(environment, "environment must not be null");
        this.maxDeploymentsPerWindow = maxDeploymentsPerWindow;
        this.windowDuration = windowDuration;
        this.actionOnExceed = actionOnExceed != null ? actionOnExceed : ThrottleAction.REJECT;
    }

    public String getEnvironment() { return environment; }
    public int getMaxDeploymentsPerWindow() { return maxDeploymentsPerWindow; }
    public Duration getWindowDuration() { return windowDuration; }
    public ThrottleAction getActionOnExceed() { return actionOnExceed; }

    public boolean isWindowExpired(Instant windowStart) {
        return Instant.now().isAfter(windowStart.plus(windowDuration));
    }

    @Override
    public String toString() {
        return "DeploymentThrottle{env='" + environment + "', max=" + maxDeploymentsPerWindow +
               ", window=" + windowDuration + ", action=" + actionOnExceed + "}";
    }
}
