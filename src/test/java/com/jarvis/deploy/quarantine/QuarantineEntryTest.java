package com.jarvis.deploy.quarantine;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class QuarantineEntryTest {

    @Test
    void shouldCreateEntryWithCorrectFields() {
        QuarantineEntry entry = new QuarantineEntry("deploy-1", "staging", QuarantineReason.HEALTH_CHECK_FAILED, "Health endpoint returned 503");

        assertEquals("deploy-1", entry.getDeploymentId());
        assertEquals("staging", entry.getEnvironment());
        assertEquals(QuarantineReason.HEALTH_CHECK_FAILED, entry.getReason());
        assertEquals("Health endpoint returned 503", entry.getDetails());
        assertFalse(entry.isReleased());
        assertNotNull(entry.getQuarantinedAt());
        assertTrue(entry.getQuarantinedAt().isBefore(Instant.now().plusSeconds(1)));
    }

    @Test
    void shouldReleaseEntry() {
        QuarantineEntry entry = new QuarantineEntry("deploy-2", "prod", QuarantineReason.MANUAL_OVERRIDE, null);
        assertFalse(entry.isReleased());

        entry.release();

        assertTrue(entry.isReleased());
    }

    @Test
    void shouldThrowOnNullDeploymentId() {
        assertThrows(NullPointerException.class,
                () -> new QuarantineEntry(null, "staging", QuarantineReason.COMPLIANCE_VIOLATION, "details"));
    }

    @Test
    void shouldThrowOnNullEnvironment() {
        assertThrows(NullPointerException.class,
                () -> new QuarantineEntry("deploy-3", null, QuarantineReason.COMPLIANCE_VIOLATION, "details"));
    }

    @Test
    void shouldThrowOnNullReason() {
        assertThrows(NullPointerException.class,
                () -> new QuarantineEntry("deploy-4", "dev", null, "details"));
    }

    @Test
    void toStringShouldContainKeyFields() {
        QuarantineEntry entry = new QuarantineEntry("deploy-5", "prod", QuarantineReason.CIRCUIT_BREAKER_OPEN, "open");
        String str = entry.toString();
        assertTrue(str.contains("deploy-5"));
        assertTrue(str.contains("prod"));
        assertTrue(str.contains("CIRCUIT_BREAKER_OPEN"));
    }
}
