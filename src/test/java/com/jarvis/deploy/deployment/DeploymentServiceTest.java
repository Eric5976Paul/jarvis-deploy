package com.jarvis.deploy.deployment;

import com.jarvis.deploy.config.DeploymentConfig;
import org.junit.jupiter.api.*;
import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DeploymentServiceTest {

    private DeploymentService service;
    private DeploymentHistory history;
    private Path tempFile;

    @BeforeEach
    void setUp() throws IOException {
        tempFile = Files.createTempFile("jarvis-svc-history", ".log");
        history = new DeploymentHistory(tempFile.toString());
        DeploymentConfig config = new DeploymentConfig();
        service = new DeploymentService(config, history);
    }

    @AfterEach
    void tearDown() throws IOException {
        Files.deleteIfExists(tempFile);
    }

    @Test
    void deploySuccessRecordsHistory() {
        boolean result = service.deploy("staging", "1.0.0", "/artifacts/app-1.0.0.jar");
        assertTrue(result);
        List<DeploymentRecord> records = history.getHistory("staging");
        assertEquals(1, records.size());
        assertEquals(DeploymentRecord.DeploymentStatus.SUCCESS, records.get(0).getStatus());
    }

    @Test
    void deployFailsWithBlankArtifact() {
        boolean result = service.deploy("staging", "1.0.0", "");
        assertFalse(result);
        List<DeploymentRecord> records = history.getHistory("staging");
        assertEquals(1, records.size());
        assertEquals(DeploymentRecord.DeploymentStatus.FAILED, records.get(0).getStatus());
    }

    @Test
    void rollbackSucceedsWhenPreviousDeploymentExists() {
        history.record(new DeploymentRecord("staging", "1.0.0", "/artifacts/app-1.0.0.jar",
                LocalDateTime.now().minusDays(1), DeploymentRecord.DeploymentStatus.SUCCESS));
        history.record(new DeploymentRecord("staging", "1.1.0", "/artifacts/app-1.1.0.jar",
                LocalDateTime.now(), DeploymentRecord.DeploymentStatus.SUCCESS));

        boolean result = service.rollback("staging");
        assertTrue(result);
    }

    @Test
    void rollbackFailsWhenNoPreviousDeployment() {
        history.record(new DeploymentRecord("staging", "1.0.0", "/artifacts/app-1.0.0.jar",
                LocalDateTime.now(), DeploymentRecord.DeploymentStatus.SUCCESS));

        boolean result = service.rollback("staging");
        assertFalse(result);
    }
}
