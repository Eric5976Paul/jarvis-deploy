package com.jarvis.deploy.drain;

import java.time.Duration;
import java.util.Objects;

/**
 * Represents a request to drain an environment before deployment.
 * Draining ensures in-flight requests are completed before stopping the app.
 */
public class DrainRequest {

    private final String environment;
    private final String appName;
    private final Duration timeout;
    private final boolean forceOnTimeout;

    public DrainRequest(String environment, String appName, Duration timeout, boolean forceOnTimeout) {
        this.environment = Objects.requireNonNull(environment, "environment must not be null");
        this.appName = Objects.requireNonNull(appName, "appName must not be null");
        this.timeout = Objects.requireNonNull(timeout, "timeout must not be null");
        this.forceOnTimeout = forceOnTimeout;
    }

    public String getEnvironment() {
        return environment;
    }

    public String getAppName() {
        return appName;
    }

    public Duration getTimeout() {
        return timeout;
    }

    public boolean isForceOnTimeout() {
        return forceOnTimeout;
    }

    @Override
    public String toString() {
        return "DrainRequest{environment='" + environment + "', appName='" + appName +
                "', timeout=" + timeout + ", forceOnTimeout=" + forceOnTimeout + "}";
    }
}
