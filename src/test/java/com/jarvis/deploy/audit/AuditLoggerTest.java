package com.jarvis.deploy.audit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AuditLoggerTest {

    private AuditLogger logger;

    @BeforeEach
    void setUp() {
        logger = new AuditLogger();
    }

    @Test
    void shouldLogAndRetrieveEvents() {
        logger.log(AuditEvent.EventType.DEPLOY_STARTED, "production", "1.0.0", "alice", "Starting");
        logger.log(AuditEvent.EventType.DEPLOY_SUCCEEDED, "production", "1.0.0", "alice", "Done");

        List<AuditEvent> events = logger.getEvents();
        assertEquals(2, events.size());
        assertEquals(AuditEvent.EventType.DEPLOY_STARTED, events.get(0).getType());
        assertEquals(AuditEvent.EventType.DEPLOY_SUCCEEDED, events.get(1).getType());
    }

    @Test
    void shouldFilterEventsByEnvironment() {
        logger.log(AuditEvent.EventType.DEPLOY_STARTED, "production", "1.0.0", "alice", "prod deploy");
        logger.log(AuditEvent.EventType.DEPLOY_STARTED, "staging", "1.0.0", "bob", "staging deploy");
        logger.log(AuditEvent.EventType.DEPLOY_SUCCEEDED, "production", "1.0.0", "alice", "prod done");

        List<AuditEvent> prodEvents = logger.getEventsForEnvironment("production");
        assertEquals(2, prodEvents.size());
        prodEvents.forEach(e -> assertEquals("production", e.getEnvironment()));
    }

    @Test
    void shouldReturnEmptyListForUnknownEnvironment() {
        logger.log(AuditEvent.EventType.DEPLOY_STARTED, "staging", "1.0.0", "alice", "msg");
        assertTrue(logger.getEventsForEnvironment("production").isEmpty());
    }

    @Test
    void shouldClearAllEvents() {
        logger.log(AuditEvent.EventType.LOCK_ACQUIRED, "dev", "1.0.0", "system", "lock");
        logger.clear();
        assertTrue(logger.getEvents().isEmpty());
    }

    @Test
    void shouldIgnoreNullEvent() {
        logger.log(null);
        assertTrue(logger.getEvents().isEmpty());
    }

    @Test
    void shouldPersistEventsToFile(@TempDir Path tempDir) throws IOException {
        Path logFile = tempDir.resolve("audit.log");
        AuditLogger fileLogger = new AuditLogger(logFile);

        fileLogger.log(AuditEvent.EventType.ROLLBACK_STARTED, "production", "0.9.0", "alice", "Rolling back");
        fileLogger.log(AuditEvent.EventType.ROLLBACK_SUCCEEDED, "production", "0.9.0", "alice", "Done");

        assertTrue(Files.exists(logFile));
        List<String> lines = Files.readAllLines(logFile);
        assertEquals(2, lines.size());
        assertTrue(lines.get(0).contains("ROLLBACK_STARTED"));
        assertTrue(lines.get(1).contains("ROLLBACK_SUCCEEDED"));
    }

    @Test
    void shouldRecordCorrectMetadataOnLoggedEvent() {
        logger.log(AuditEvent.EventType.DEPLOY_STARTED, "staging", "2.1.0", "bob", "Deploying 2.1.0");

        AuditEvent event = logger.getEvents().get(0);
        assertEquals(AuditEvent.EventType.DEPLOY_STARTED, event.getType());
        assertEquals("staging", event.getEnvironment());
        assertEquals("2.1.0", event.getVersion());
        assertEquals("bob", event.getInitiator());
        assertEquals("Deploying 2.1.0", event.getMessage());
        assertNotNull(event.getTimestamp());
    }
}
