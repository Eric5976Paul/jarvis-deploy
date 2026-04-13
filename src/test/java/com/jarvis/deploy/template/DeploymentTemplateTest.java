package com.jarvis.deploy.template;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DeploymentTemplateTest {

    @Test
    void shouldBuildTemplateWithRequiredFields() {
        DeploymentTemplate template = DeploymentTemplate.builder("api-service", "staging").build();

        assertEquals("api-service", template.getName());
        assertEquals("staging", template.getEnvironment());
    }

    @Test
    void shouldApplyDefaultValues() {
        DeploymentTemplate template = DeploymentTemplate.builder("svc", "prod").build();

        assertEquals("*.jar", template.getArtifactPattern());
        assertEquals(3, template.getMaxRetries());
        assertTrue(template.isHealthCheckEnabled());
        assertTrue(template.getDefaultProperties().isEmpty());
    }

    @Test
    void shouldOverrideDefaultsViaBuilder() {
        DeploymentTemplate template = DeploymentTemplate.builder("worker", "dev")
                .artifactPattern("worker-*.jar")
                .maxRetries(5)
                .healthCheckEnabled(false)
                .property("JVM_OPTS", "-Xmx512m")
                .property("LOG_LEVEL", "DEBUG")
                .build();

        assertEquals("worker-*.jar", template.getArtifactPattern());
        assertEquals(5, template.getMaxRetries());
        assertFalse(template.isHealthCheckEnabled());
        assertEquals("-Xmx512m", template.getDefaultProperties().get("JVM_OPTS"));
        assertEquals("DEBUG", template.getDefaultProperties().get("LOG_LEVEL"));
    }

    @Test
    void shouldReturnImmutableProperties() {
        DeploymentTemplate template = DeploymentTemplate.builder("svc", "prod")
                .property("KEY", "VALUE")
                .build();

        assertThrows(UnsupportedOperationException.class, () ->
                template.getDefaultProperties().put("NEW_KEY", "NEW_VALUE")
        );
    }

    @Test
    void shouldThrowWhenNameIsNull() {
        assertThrows(NullPointerException.class, () ->
                DeploymentTemplate.builder(null, "prod").build()
        );
    }

    @Test
    void shouldThrowWhenEnvironmentIsNull() {
        assertThrows(NullPointerException.class, () ->
                DeploymentTemplate.builder("svc", null).build()
        );
    }

    @Test
    void shouldProduceReadableToString() {
        DeploymentTemplate template = DeploymentTemplate.builder("api", "staging")
                .maxRetries(2)
                .healthCheckEnabled(true)
                .build();

        String result = template.toString();
        assertTrue(result.contains("api"));
        assertTrue(result.contains("staging"));
        assertTrue(result.contains("2"));
    }
}
