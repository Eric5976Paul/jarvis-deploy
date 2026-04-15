package com.jarvis.deploy.variable;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DeploymentVariableTest {

    @Test
    void shouldCreateVariableWithDefaults() {
        DeploymentVariable var = new DeploymentVariable("KEY", "value", VariableScope.GLOBAL, null);
        assertEquals("KEY", var.getKey());
        assertEquals("value", var.getValue());
        assertEquals(VariableScope.GLOBAL, var.getScope());
        assertFalse(var.isSensitive());
        assertNotNull(var.getCreatedAt());
        assertNotNull(var.getUpdatedAt());
    }

    @Test
    void shouldMaskDisplayValueWhenSensitive() {
        DeploymentVariable var = new DeploymentVariable("SECRET", "my-secret-token", VariableScope.ENVIRONMENT, "prod");
        var.setSensitive(true);
        assertEquals("****", var.getDisplayValue());
        assertEquals("my-secret-token", var.getValue());
    }

    @Test
    void shouldShowDisplayValueWhenNotSensitive() {
        DeploymentVariable var = new DeploymentVariable("PORT", "8080", VariableScope.ENVIRONMENT, "staging");
        assertEquals("8080", var.getDisplayValue());
    }

    @Test
    void shouldUpdateValueAndTimestamp() throws InterruptedException {
        DeploymentVariable var = new DeploymentVariable("KEY", "old", VariableScope.GLOBAL, null);
        Thread.sleep(5);
        var.setValue("new");
        assertEquals("new", var.getValue());
        assertTrue(var.getUpdatedAt().isAfter(var.getCreatedAt()));
    }

    @Test
    void shouldBeEqualWhenKeyScopeAndEnvironmentMatch() {
        DeploymentVariable v1 = new DeploymentVariable("KEY", "a", VariableScope.ENVIRONMENT, "prod");
        DeploymentVariable v2 = new DeploymentVariable("KEY", "b", VariableScope.ENVIRONMENT, "prod");
        assertEquals(v1, v2);
        assertEquals(v1.hashCode(), v2.hashCode());
    }

    @Test
    void shouldNotBeEqualWhenScopeDiffers() {
        DeploymentVariable v1 = new DeploymentVariable("KEY", "val", VariableScope.GLOBAL, null);
        DeploymentVariable v2 = new DeploymentVariable("KEY", "val", VariableScope.ENVIRONMENT, "prod");
        assertNotEquals(v1, v2);
    }

    @Test
    void shouldThrowOnNullKey() {
        assertThrows(NullPointerException.class, () ->
            new DeploymentVariable(null, "value", VariableScope.GLOBAL, null));
    }

    @Test
    void shouldVerifyScopeSpecificity() {
        assertTrue(VariableScope.DEPLOYMENT.isMoreSpecificThan(VariableScope.ENVIRONMENT));
        assertTrue(VariableScope.ENVIRONMENT.isMoreSpecificThan(VariableScope.GLOBAL));
        assertFalse(VariableScope.GLOBAL.isMoreSpecificThan(VariableScope.ENVIRONMENT));
    }
}
