package com.jarvis.deploy.env;

import com.jarvis.deploy.config.DeploymentConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Validates that a target environment is properly configured before deployment.
 */
public class EnvironmentValidator {

    private static final Set<String> KNOWN_ENVIRONMENTS = Set.of("dev", "staging", "prod", "qa");

    private final DeploymentConfig config;

    public EnvironmentValidator(DeploymentConfig config) {
        this.config = config;
    }

    /**
     * Validates the given environment name and its configuration.
     *
     * @param environment the target environment name
     * @return a ValidationResult containing any errors found
     */
    public ValidationResult validate(String environment) {
        List<String> errors = new ArrayList<>();

        if (environment == null || environment.isBlank()) {
            errors.add("Environment name must not be blank.");
            return new ValidationResult(false, errors);
        }

        if (!KNOWN_ENVIRONMENTS.contains(environment.toLowerCase())) {
            errors.add("Unknown environment '" + environment + "'. Known environments: " + KNOWN_ENVIRONMENTS);
        }

        if (config == null) {
            errors.add("Deployment configuration is missing.");
            return new ValidationResult(false, errors);
        }

        if (config.getAppName() == null || config.getAppName().isBlank()) {
            errors.add("Application name is not configured.");
        }

        if (config.getJarPath() == null || config.getJarPath().isBlank()) {
            errors.add("JAR path is not configured.");
        }

        if (config.getEnvironments() == null || !config.getEnvironments().containsKey(environment.toLowerCase())) {
            errors.add("No configuration block found for environment '" + environment + "'.");
        }

        return new ValidationResult(errors.isEmpty(), errors);
    }
}
