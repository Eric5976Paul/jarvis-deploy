package com.jarvis.deploy.deployment;

import org.junit.jupiter.api.*;
import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class DeploymentHistoryTest {

    private Path tempFile;
    private DeploymentHistory history;

    @BeforeEach
    void setUp() throws IOException {
        tempFile = Files.createTempFile("jarvis-history", ".log");
        history = new DeploymentHistory(tempFile.toString());
    }

    @AfterEach
    void tearDown() throws IOException {
        Files.deleteIfExists(tempFile);
    }

    @Test
    void recordAndRetrieveByEnvironment() {
        history.record(new DeploymentRecord("staging", "1.0.0", "/app/app-1.0.0.jar",
                LocalDateTime.now(), DeploymentRecord.DeploymentStatus.SUCCESS));
        history.record(new DeploymentRecord("prod", "1.0.0", "/app/app-1.0.0.jar",
                LocalDateTime.now(), DeploymentRecord.DeploymentStatus.SUCCESS));

        List<DeploymentRecord> stagingRecords = history.getHistory("staging");
        assertEquals(1, stagingRecords.size());
        assertEquals("staging", stagingRecords.get(0).getEnvironment());
    }

    @Test
    void getLastSuccessfulReturnsNewest() {
        LocalDateTime older = LocalDateTime.now().minusDays(1);
        LocalDateTime newer = LocalDateTime.now();

        history.record(new DeploymentRecord("staging", "1.0.0", "/app/app-1.0.0.jar",
                older, DeploymentRecord.DeploymentStatus.SUCCESS));
        history.record(new DeploymentRecord("staging", "1.1.0", "/app/app-1.1.0.jar",
                newer, DeploymentRecord.DeploymentStatus.SUCCESS));

        Optional<DeploymentRecord> last = history.getLastSuccessful("staging");
        assertTrue(last.isPresent());
        assertEquals("1.1.0", last.get().getVersion());
    }

    @Test
    void getLastSuccessfulIgnoresFailures() {
        history.record(new DeploymentRecord("staging", "1.0.0", "/app/app-1.0.0.jar",
                LocalDateTime.now().minusDays(1), DeploymentRecord.DeploymentStatus.SUCCESS));
        history.record(new DeploymentRecord("staging", "1.1.0", "/app/app-1.1.0.jar",
                LocalDateTime.now(), DeploymentRecord.DeploymentStatus.FAILED));

        Optional<DeploymentRecord> last = history.getLastSuccessful("staging");
        assertTrue(last.isPresent());
        assertEquals("1.0.0", last.get().getVersion());
    }

    @Test
    void emptyHistoryReturnsEmptyOptional() {
        Optional<DeploymentRecord> last = history.getLastSuccessful("prod");
        assertFalse(last.isPresent());
    }
}
