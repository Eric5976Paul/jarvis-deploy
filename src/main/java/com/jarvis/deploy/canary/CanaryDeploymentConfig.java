package com.jarvis.deploy.canary;

/**
 * Configuration for a canary deployment, defining traffic split and evaluation window.
 */
public class CanaryDeploymentConfig {

    private final String environment;
    private final String appName;
    private final String canaryVersion;
    private final String stableVersion;
    private final int canaryTrafficPercent;
    private final long evaluationWindowSeconds;

    public CanaryDeploymentConfig(String environment, String appName,
                                   String canaryVersion, String stableVersion,
                                   int canaryTrafficPercent, long evaluationWindowSeconds) {
        if (canaryTrafficPercent < 0 || canaryTrafficPercent > 100) {
            throw new IllegalArgumentException("canaryTrafficPercent must be between 0 and 100");
        }
        this.environment = environment;
        this.appName = appName;
        this.canaryVersion = canaryVersion;
        this.stableVersion = stableVersion;
        this.canaryTrafficPercent = canaryTrafficPercent;
        this.evaluationWindowSeconds = evaluationWindowSeconds;
    }

    public String getEnvironment() { return environment; }
    public String getAppName() { return appName; }
    public String getCanaryVersion() { return canaryVersion; }
    public String getStableVersion() { return stableVersion; }
    public int getCanaryTrafficPercent() { return canaryTrafficPercent; }
    public long getEvaluationWindowSeconds() { return evaluationWindowSeconds; }

    @Override
    public String toString() {
        return String.format("CanaryDeploymentConfig{env=%s, app=%s, canary=%s, stable=%s, traffic=%d%%}",
                environment, appName, canaryVersion, stableVersion, canaryTrafficPercent);
    }
}
