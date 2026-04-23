package com.jarvis.deploy.baseline;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class BaselineServiceTest {

    private BaselineService service;

    @BeforeEach
    void setUp() {
        service = new BaselineService();
    }

    @Test
    void captureStoresBaselineForEnvironment() {
        Map<String, String> props = Map.of("version", "1.0.0", "replicas", "3");
        DeploymentBaseline baseline = service.capture("staging", "1.0.0", props, "ci-bot");

        assertNotNull(baseline.getBaselineId());
        assertEquals("staging", baseline.getEnvironment());
        assertEquals("1.0.0", baseline.getVersion());
        assertEquals("ci-bot", baseline.getCapturedBy());
        assertTrue(service.hasBaseline("staging"));
    }

    @Test
    void getBaselineReturnsEmptyWhenNotPresent() {
        Optional<DeploymentBaseline> result = service.getBaseline("production");
        assertTrue(result.isEmpty());
    }

    @Test
    void detectDriftReturnsNoDriftWhenPropertiesMatch() {
        Map<String, String> props = Map.of("replicas", "3", "memory", "512m");
        service.capture("dev", "2.0.0", props, "admin");

        BaselineDriftResult result = service.detectDrift("dev", new HashMap<>(props));

        assertTrue(result.isBaselineFound());
        assertFalse(result.hasDrift());
        assertEquals(0, result.getDriftCount());
    }

    @Test
    void detectDriftIdentifiesChangedProperties() {
        Map<String, String> baseline = Map.of("replicas", "3", "memory", "512m");
        service.capture("prod", "3.0.0", baseline, "admin");

        Map<String, String> current = Map.of("replicas", "5", "memory", "512m");
        BaselineDriftResult result = service.detectDrift("prod", current);

        assertTrue(result.hasDrift());
        assertTrue(result.getDriftedKeys().contains("replicas"));
        assertEquals(1, result.getDriftCount());
    }

    @Test
    void detectDriftReturnsNoBaselineWhenMissing() {
        BaselineDriftResult result = service.detectDrift("unknown-env", Map.of("key", "val"));
        assertFalse(result.isBaselineFound());
        assertFalse(result.hasDrift());
    }

    @Test
    void removeBaselineReturnsTrueWhenExists() {
        service.capture("qa", "1.1.0", Map.of(), "tester");
        assertTrue(service.removeBaseline("qa"));
        assertFalse(service.hasBaseline("qa"));
    }

    @Test
    void removeBaselineReturnsFalseWhenNotExists() {
        assertFalse(service.removeBaseline("nonexistent"));
    }

    @Test
    void listAllReturnsCapturedBaselines() {
        service.capture("env1", "1.0.0", Map.of(), "user");
        service.capture("env2", "2.0.0", Map.of(), "user");
        assertEquals(2, service.listAll().size());
    }

    @Test
    void captureOverwritesPreviousBaselineForSameEnvironment() {
        service.capture("staging", "1.0.0", Map.of("k", "v1"), "user");
        DeploymentBaseline updated = service.capture("staging", "2.0.0", Map.of("k", "v2"), "user");

        Optional<DeploymentBaseline> stored = service.getBaseline("staging");
        assertTrue(stored.isPresent());
        assertEquals(updated.getBaselineId(), stored.get().getBaselineId());
        assertEquals("2.0.0", stored.get().getVersion());
    }
}
