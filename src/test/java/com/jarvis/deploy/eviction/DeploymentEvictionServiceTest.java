package com.jarvis.deploy.eviction;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DeploymentEvictionServiceTest {

    private DeploymentEvictionService service;

    @BeforeEach
    void setUp() {
        service = new DeploymentEvictionService(60);
    }

    @Test
    void shouldTrackDeployment() {
        service.track("dep-001", "staging");
        assertTrue(service.isTracked("dep-001"));
        assertEquals(1, service.trackedCount());
    }

    @Test
    void shouldNotTrackBlankId() {
        assertThrows(IllegalArgumentException.class, () -> service.track("", "staging"));
        assertThrows(IllegalArgumentException.class, () -> service.track(null, "staging"));
    }

    @Test
    void shouldRejectNonPositiveTtl() {
        assertThrows(IllegalArgumentException.class, () -> new DeploymentEvictionService(0));
        assertThrows(IllegalArgumentException.class, () -> new DeploymentEvictionService(-5));
    }

    @Test
    void shouldEvictExpiredEntries() throws InterruptedException {
        service.track("dep-expired", "prod", 1);
        service.track("dep-active", "prod", 120);

        Thread.sleep(1100);

        EvictionResult result = service.evictExpired();
        assertTrue(result.getEvictedIds().contains("dep-expired"));
        assertFalse(result.getEvictedIds().contains("dep-active"));
        assertFalse(service.isTracked("dep-expired"));
        assertTrue(service.isTracked("dep-active"));
    }

    @Test
    void shouldEvictByEnvironment() {
        service.track("dep-a", "staging");
        service.track("dep-b", "staging");
        service.track("dep-c", "prod");

        EvictionResult result = service.evictByEnvironment("staging");
        assertEquals(2, result.getEvictedIds().size());
        assertTrue(result.getEvictedIds().containsAll(List.of("dep-a", "dep-b")));
        assertFalse(service.isTracked("dep-a"));
        assertFalse(service.isTracked("dep-b"));
        assertTrue(service.isTracked("dep-c"));
    }

    @Test
    void shouldUntrackDeployment() {
        service.track("dep-001", "dev");
        service.untrack("dep-001");
        assertFalse(service.isTracked("dep-001"));
        assertEquals(0, service.trackedCount());
    }

    @Test
    void shouldReturnEmptyEvictionWhenNothingExpired() {
        service.track("dep-fresh", "dev", 300);
        EvictionResult result = service.evictExpired();
        assertTrue(result.getEvictedIds().isEmpty());
        assertNotNull(result.getEvictedAt());
    }
}
