package com.jarvis.deploy.audit;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class AuditEventTest {

    @Test
    void shouldCreateEventWithAllFields() {
        AuditEvent event = new AuditEvent(
                AuditEvent.EventType.DEPLOY_STARTED, "production", "1.2.3", "alice", "Deployment initiated");

        assertEquals(AuditEvent.EventType.DEPLOY_STARTED, event.getType());
        assertEquals("production", event.getEnvironment());
        assertEquals("1.2.3", event.getVersion());
        assertEquals("alice", event.getActor());
        assertEquals("Deployment initiated", event.getMessage());
        assertNotNull(event.getTimestamp());
        assertTrue(event.getTimestamp().isBefore(Instant.now().plusSeconds(1)));
    }

    @Test
    void shouldDefaultActorToSystemWhenNull() {
        AuditEvent event = new AuditEvent(
                AuditEvent.EventType.HEALTH_CHECK_PASSED, "staging", "1.0.0", null, null);

        assertEquals("system", event.getActor());
        assertEquals("", event.getMessage());
    }

    @Test
    void shouldThrowWhenTypeIsNull() {
        assertThrows(NullPointerException.class, () ->
                new AuditEvent(null, "production", "1.0.0", "alice", "msg"));
    }

    @Test
    void shouldThrowWhenEnvironmentIsNull() {
        assertThrows(NullPointerException.class, () ->
                new AuditEvent(AuditEvent.EventType.DEPLOY_FAILED, null, "1.0.0", "alice", "msg"));
    }

    @Test
    void toStringShouldContainKeyFields() {
        AuditEvent event = new AuditEvent(
                AuditEvent.EventType.ROLLBACK_SUCCEEDED, "dev", "0.9.0", "bob", "Rolled back successfully");
        String str = event.toString();

        assertTrue(str.contains("ROLLBACK_SUCCEEDED"));
        assertTrue(str.contains("dev"));
        assertTrue(str.contains("0.9.0"));
        assertTrue(str.contains("bob"));
        assertTrue(str.contains("Rolled back successfully"));
    }
}
