package com.jarvis.deploy.variable;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class VariableRegistryTest {

    private VariableRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new VariableRegistry();
    }

    @Test
    void shouldRegisterAndResolveGlobalVariable() {
        registry.set("DB_URL", "jdbc:h2:mem:test", VariableScope.GLOBAL, null);
        Optional<String> value = registry.resolveValue("DB_URL", "staging");
        assertTrue(value.isPresent());
        assertEquals("jdbc:h2:mem:test", value.get());
    }

    @Test
    void shouldPreferEnvironmentScopeOverGlobal() {
        registry.set("LOG_LEVEL", "INFO", VariableScope.GLOBAL, null);
        registry.set("LOG_LEVEL", "DEBUG", VariableScope.ENVIRONMENT, "staging");
        Optional<String> value = registry.resolveValue("LOG_LEVEL", "staging");
        assertTrue(value.isPresent());
        assertEquals("DEBUG", value.get());
    }

    @Test
    void shouldPreferDeploymentScopeOverEnvironment() {
        registry.set("REPLICAS", "2", VariableScope.ENVIRONMENT, "prod");
        registry.set("REPLICAS", "5", VariableScope.DEPLOYMENT, "prod");
        Optional<String> value = registry.resolveValue("REPLICAS", "prod");
        assertTrue(value.isPresent());
        assertEquals("5", value.get());
    }

    @Test
    void shouldReturnEmptyWhenVariableNotFound() {
        Optional<String> value = registry.resolveValue("MISSING_KEY", "prod");
        assertFalse(value.isPresent());
    }

    @Test
    void shouldListVariablesByEnvironmentIncludingGlobals() {
        registry.set("GLOBAL_KEY", "global_val", VariableScope.GLOBAL, null);
        registry.set("ENV_KEY", "env_val", VariableScope.ENVIRONMENT, "staging");
        registry.set("OTHER_KEY", "other_val", VariableScope.ENVIRONMENT, "prod");

        List<DeploymentVariable> vars = registry.listByEnvironment("staging");
        assertEquals(2, vars.size());
        assertTrue(vars.stream().anyMatch(v -> v.getKey().equals("GLOBAL_KEY")));
        assertTrue(vars.stream().anyMatch(v -> v.getKey().equals("ENV_KEY")));
    }

    @Test
    void shouldUpdateExistingVariable() {
        registry.set("PORT", "8080", VariableScope.ENVIRONMENT, "dev");
        registry.set("PORT", "9090", VariableScope.ENVIRONMENT, "dev");
        assertEquals("9090", registry.resolveValue("PORT", "dev").orElse(null));
        assertEquals(1, registry.size());
    }

    @Test
    void shouldRemoveVariable() {
        registry.set("TEMP", "value", VariableScope.GLOBAL, null);
        boolean removed = registry.remove("TEMP", VariableScope.GLOBAL, null);
        assertTrue(removed);
        assertFalse(registry.resolveValue("TEMP", "any").isPresent());
    }

    @Test
    void shouldReturnFalseWhenRemovingNonExistentVariable() {
        boolean removed = registry.remove("NON_EXISTENT", VariableScope.GLOBAL, null);
        assertFalse(removed);
    }
}
