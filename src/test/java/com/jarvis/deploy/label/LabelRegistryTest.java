package com.jarvis.deploy.label;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LabelRegistryTest {
    private LabelRegistry registry;

    @BeforeEach
    void setUp() { registry = new LabelRegistry(); }

    @Test
    void putAndGet() {
        registry.put(new DeploymentLabel("env", "prod"));
        assertTrue(registry.get("env").isPresent());
        assertEquals("prod", registry.get("env").get().getValue());
    }

    @Test
    void getAbsentReturnsEmpty() {
        assertTrue(registry.get("missing").isEmpty());
    }

    @Test
    void hasLabelWithValue() {
        registry.put(new DeploymentLabel("team", "platform"));
        assertTrue(registry.hasLabelWithValue("team", "platform"));
        assertFalse(registry.hasLabelWithValue("team", "other"));
    }

    @Test
    void remove() {
        registry.put(new DeploymentLabel("tier", "backend"));
        assertTrue(registry.remove("tier"));
        assertFalse(registry.hasLabel("tier"));
        assertFalse(registry.remove("tier"));
    }

    @Test
    void propagatableFiltered() {
        registry.put(new DeploymentLabel("a", "1", true));
        registry.put(new DeploymentLabel("b", "2", false));
        List<DeploymentLabel> prop = registry.propagatable();
        assertEquals(1, prop.size());
        assertEquals("a", prop.get(0).getKey());
    }

    @Test
    void mergeDoesNotOverwrite() {
        registry.put(new DeploymentLabel("env", "prod"));
        LabelRegistry other = new LabelRegistry();
        other.put(new DeploymentLabel("env", "staging"));
        other.put(new DeploymentLabel("region", "us-east"));
        registry.merge(other);
        assertEquals("prod", registry.get("env").get().getValue());
        assertTrue(registry.hasLabel("region"));
    }

    @Test
    void labelKeyBlankThrows() {
        assertThrows(IllegalArgumentException.class, () -> new DeploymentLabel("", "v"));
    }
}
