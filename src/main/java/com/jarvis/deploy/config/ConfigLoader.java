package com.jarvis.deploy.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Loads deployment configuration from a jarvis.yml file in the current directory
 * or a specified path. Falls back to defaults when fields are missing.
 */
public class ConfigLoader {

    private static final String DEFAULT_CONFIG_FILE = "jarvis.yml";
    private final ObjectMapper yamlMapper;

    public ConfigLoader() {
        this.yamlMapper = new ObjectMapper(new YAMLFactory());
    }

    /**
     * Loads config from the default jarvis.yml in the working directory.
     */
    public DeploymentConfig load() throws IOException {
        return load(Paths.get(System.getProperty("user.dir"), DEFAULT_CONFIG_FILE));
    }

    /**
     * Loads config from a specific file path.
     */
    public DeploymentConfig load(Path configPath) throws IOException {
        File configFile = configPath.toFile();
        if (!configFile.exists()) {
            throw new IOException("Configuration file not found: " + configPath.toAbsolutePath());
        }
        DeploymentConfig config = yamlMapper.readValue(configFile, DeploymentConfig.class);
        validate(config);
        applyDefaults(config);
        return config;
    }

    private void validate(DeploymentConfig config) {
        if (config.getAppName() == null || config.getAppName().isBlank()) {
            throw new IllegalArgumentException("'appName' is required in jarvis.yml");
        }
        if (config.getArtifactPath() == null || config.getArtifactPath().isBlank()) {
            throw new IllegalArgumentException("'artifactPath' is required in jarvis.yml");
        }
    }

    private void applyDefaults(DeploymentConfig config) {
        if (config.getEnvironment() == null || config.getEnvironment().isBlank()) {
            config.setEnvironment("default");
        }
        if (config.getDeployDir() == null || config.getDeployDir().isBlank()) {
            config.setDeployDir("/opt/jarvis/deployments/" + config.getAppName());
        }
    }
}
