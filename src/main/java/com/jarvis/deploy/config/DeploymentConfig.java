package com.jarvis.deploy.config;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents the deployment configuration loaded from jarvis.yml or CLI arguments.
 */
public class DeploymentConfig {

    private String appName;
    private String environment;
    private String artifactPath;
    private String deployDir;
    private int maxRollbackVersions = 5;
    private Map<String, String> envVars = new HashMap<>();

    public DeploymentConfig() {}

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public String getArtifactPath() {
        return artifactPath;
    }

    public void setArtifactPath(String artifactPath) {
        this.artifactPath = artifactPath;
    }

    public String getDeployDir() {
        return deployDir;
    }

    public void setDeployDir(String deployDir) {
        this.deployDir = deployDir;
    }

    public int getMaxRollbackVersions() {
        return maxRollbackVersions;
    }

    public void setMaxRollbackVersions(int maxRollbackVersions) {
        this.maxRollbackVersions = maxRollbackVersions;
    }

    public Map<String, String> getEnvVars() {
        return envVars;
    }

    public void setEnvVars(Map<String, String> envVars) {
        this.envVars = envVars;
    }

    @Override
    public String toString() {
        return "DeploymentConfig{" +
                "appName='" + appName + '\'' +
                ", environment='" + environment + '\'' +
                ", artifactPath='" + artifactPath + '\'' +
                ", deployDir='" + deployDir + '\'' +
                ", maxRollbackVersions=" + maxRollbackVersions +
                '}';
    }
}
