package com.jarvis.deploy.stealth;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class StealthModeTest {

    private StealthMode buildMode(Instant expiresAt) {
        return new StealthMode(
            "dep-1", "prod",
            true, false,
            "test reason",
            Instant.now().minusSeconds(60),
            expiresAt
        );
    }

    @Test
    void isExpired_whenExpiresAtInPast_returnsTrue() {
        StealthMode mode = buildMode(Instant.now().minusSeconds(10));
        assertTrue(mode.isExpired(Instant.now()));
    }

    @Test
    void isExpired_whenExpiresAtInFuture_returnsFalse() {
        StealthMode mode = buildMode(Instant.now().plusSeconds(3600));
        assertFalse(mode.isExpired(Instant.now()));
    }

    @Test
    void isExpired_whenExpiresAtIsNull_returnsFalse() {
        StealthMode mode = buildMode(null);
        assertFalse(mode.isExpired(Instant.now()));
    }

    @Test
    void isActive_whenNotExpired_returnsTrue() {
        StealthMode mode = buildMode(Instant.now().plusSeconds(3600));
        assertTrue(mode.isActive(Instant.now()));
    }

    @Test
    void constructorNullDeploymentId_throwsNullPointer() {
        assertThrows(NullPointerException.class, () ->
            new StealthMode(null, "prod", true, false, "r", Instant.now(), null));
    }

    @Test
    void constructorNullEnvironment_throwsNullPointer() {
        assertThrows(NullPointerException.class, () ->
            new StealthMode("dep-1", null, true, false, "r", Instant.now(), null));
    }

    @Test
    void getters_returnExpectedValues() {
        Instant now = Instant.now();
        Instant expires = now.plusSeconds(1800);
        StealthMode mode = new StealthMode(
            "dep-x", "staging", true, true, "maintenance", now, expires);
        assertEquals("dep-x", mode.getDeploymentId());
        assertEquals("staging", mode.getEnvironment());
        assertTrue(mode.isSuppressNotifications());
        assertTrue(mode.isSuppressAudit());
        assertEquals("maintenance", mode.getReason());
        assertEquals(now, mode.getActivatedAt());
        assertEquals(expires, mode.getExpiresAt());
    }

    @Test
    void toString_containsKeyFields() {
        StealthMode mode = buildMode(null);
        String str = mode.toString();
        assertTrue(str.contains("dep-1"));
        assertTrue(str.contains("prod"));
    }
}
