package com.jarvis.deploy.stealth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

class StealthModeServiceTest {

    private StealthModeService service;

    @BeforeEach
    void setUp() {
        service = new StealthModeService();
    }

    @Test
    void activate_shouldReturnActivatedResult() {
        StealthModeResult result = service.activate(
            "dep-1", "prod", true, false, "hotfix", Duration.ofMinutes(30));
        assertEquals(StealthModeResult.Status.ACTIVATED, result.getStatus());
        assertTrue(result.isSuccess());
        assertTrue(result.getStealthMode().isPresent());
    }

    @Test
    void activate_whenAlreadyActive_shouldReturnAlreadyActive() {
        service.activate("dep-1", "prod", true, false, "hotfix", Duration.ofMinutes(30));
        StealthModeResult result = service.activate(
            "dep-1", "prod", true, false, "hotfix again", Duration.ofMinutes(10));
        assertEquals(StealthModeResult.Status.ALREADY_ACTIVE, result.getStatus());
    }

    @Test
    void activate_withBlankDeploymentId_shouldReturnRejected() {
        StealthModeResult result = service.activate(
            "", "prod", true, false, "reason", Duration.ofMinutes(10));
        assertEquals(StealthModeResult.Status.REJECTED, result.getStatus());
    }

    @Test
    void activate_withExcessiveDuration_shouldReturnRejected() {
        StealthModeResult result = service.activate(
            "dep-2", "staging", true, true, "long", Duration.ofHours(5));
        assertEquals(StealthModeResult.Status.REJECTED, result.getStatus());
    }

    @Test
    void deactivate_shouldReturnDeactivated() {
        service.activate("dep-3", "dev", true, true, "test", Duration.ofMinutes(5));
        StealthModeResult result = service.deactivate("dep-3");
        assertEquals(StealthModeResult.Status.DEACTIVATED, result.getStatus());
        assertTrue(result.isSuccess());
    }

    @Test
    void deactivate_whenNotFound_shouldReturnNotFound() {
        StealthModeResult result = service.deactivate("nonexistent");
        assertEquals(StealthModeResult.Status.NOT_FOUND, result.getStatus());
        assertFalse(result.isSuccess());
    }

    @Test
    void isStealthActive_shouldReturnTrueWhenActive() {
        service.activate("dep-4", "prod", true, false, "reason", Duration.ofMinutes(10));
        assertTrue(service.isStealthActive("dep-4"));
    }

    @Test
    void shouldSuppressNotifications_whenActive_returnsTrue() {
        service.activate("dep-5", "prod", true, false, "r", Duration.ofMinutes(5));
        assertTrue(service.shouldSuppressNotifications("dep-5"));
        assertFalse(service.shouldSuppressAudit("dep-5"));
    }

    @Test
    void shouldSuppressAudit_whenActive_returnsTrue() {
        service.activate("dep-6", "prod", false, true, "r", Duration.ofMinutes(5));
        assertFalse(service.shouldSuppressNotifications("dep-6"));
        assertTrue(service.shouldSuppressAudit("dep-6"));
    }

    @Test
    void getActive_whenNoStealth_returnsEmpty() {
        assertTrue(service.getActive("unknown").isEmpty());
    }

    @Test
    void activate_withNullDuration_shouldSucceedWithNoExpiry() {
        StealthModeResult result = service.activate(
            "dep-7", "staging", true, true, "indefinite", null);
        assertEquals(StealthModeResult.Status.ACTIVATED, result.getStatus());
        assertTrue(result.getStealthMode().isPresent());
        assertNull(result.getStealthMode().get().getExpiresAt());
    }
}
