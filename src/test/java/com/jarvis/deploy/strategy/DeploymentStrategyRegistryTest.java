package com.jarvis.deploy.strategy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DeploymentStrategyRegistryTest {

    private DeploymentStrategyRegistry registry;
    private DeploymentStrategy mockStrategy;

    @BeforeEach
    void setUp() {
        registry = new DeploymentStrategyRegistry();
        mockStrategy = mock(DeploymentStrategy.class);
    }

    @Test
    void shouldRegisterAndFindStrategy() {
        registry.register("blue-green", mockStrategy);
        Optional<DeploymentStrategy> found = registry.find("blue-green");
        assertTrue(found.isPresent());
        assertSame(mockStrategy, found.get());
    }

    @Test
    void shouldFindCaseInsensitive() {
        registry.register("Canary", mockStrategy);
        assertTrue(registry.find("canary").isPresent());
        assertTrue(registry.find("CANARY").isPresent());
    }

    @Test
    void shouldReturnEmptyForUnknownStrategy() {
        assertTrue(registry.find("nonexistent").isEmpty());
    }

    @Test
    void shouldReturnDefaultWhenNotFound() {
        DeploymentStrategy fallback = mock(DeploymentStrategy.class);
        DeploymentStrategy result = registry.getOrDefault("missing", fallback);
        assertSame(fallback, result);
    }

    @Test
    void shouldReportRegisteredCorrectly() {
        registry.register("rolling", mockStrategy);
        assertTrue(registry.isRegistered("rolling"));
        assertFalse(registry.isRegistered("unknown"));
    }

    @Test
    void shouldListAllNames() {
        registry.register("rolling", mockStrategy);
        registry.register("canary", mockStrategy);
        assertEquals(2, registry.listNames().size());
        assertTrue(registry.listNames().contains("rolling"));
    }

    @Test
    void shouldUnregisterStrategy() {
        registry.register("rolling", mockStrategy);
        registry.unregister("rolling");
        assertFalse(registry.isRegistered("rolling"));
    }

    @Test
    void shouldThrowOnBlankName() {
        assertThrows(IllegalArgumentException.class, () -> registry.register("", mockStrategy));
        assertThrows(IllegalArgumentException.class, () -> registry.register(null, mockStrategy));
    }

    @Test
    void shouldThrowOnNullStrategy() {
        assertThrows(IllegalArgumentException.class, () -> registry.register("valid", null));
    }
}
