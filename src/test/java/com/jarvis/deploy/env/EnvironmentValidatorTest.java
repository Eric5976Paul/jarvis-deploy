package com.jarvis.deploy.env;

import com.jarvis.deploy.config.DeploymentConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class EnvironmentValidatorTest {

    private DeploymentConfig config;
    private EnvironmentValidator validator;

    @BeforeEach
    void setUp() {
        config = new DeploymentConfig();
        config.setAppName("my-app");
        config.setJarPath("/opt/apps/my-app.jar");
        config.setEnvironments(Map.of(
                "dev", Map.of("host", "localhost", "port", "8080"),
                "staging", Map.of("host", "staging.example.com", "port", "8080")
        ));
        validator = new EnvironmentValidator(config);
    }

    @Test
    void validEnvironmentPassesValidation() {
        ValidationResult result = validator.validate("dev");
        assertTrue(result.isValid());
        assertTrue(result.getErrors().isEmpty());
    }

    @Test
    void unknownEnvironmentFailsValidation() {
        ValidationResult result = validator.validate("unknown");
        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(e -> e.contains("Unknown environment")));
    }

    @Test
    void blankEnvironmentFailsValidation() {
        ValidationResult result = validator.validate("");
        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(e -> e.contains("blank")));
    }

    @Test
    void nullEnvironmentFailsValidation() {
        ValidationResult result = validator.validate(null);
        assertFalse(result.isValid());
    }

    @Test
    void missingAppNameFailsValidation() {
        config.setAppName(null);
        ValidationResult result = validator.validate("dev");
        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(e -> e.contains("Application name")));
    }

    @Test
    void environmentNotInConfigBlockFailsValidation() {
        ValidationResult result = validator.validate("prod");
        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(e -> e.contains("No configuration block")));
    }

    @Test
    void getSummaryReturnsPassedMessageWhenValid() {
        ValidationResult result = validator.validate("staging");
        assertTrue(result.getSummary().contains("passed"));
    }

    @Test
    void getSummaryReturnsFailedMessageWhenInvalid() {
        ValidationResult result = validator.validate("prod");
        assertTrue(result.getSummary().contains("failed"));
    }
}
