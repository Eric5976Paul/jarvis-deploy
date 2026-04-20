package com.jarvis.deploy.precheck;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DeploymentPreCheckServiceTest {

    private DeploymentPreCheckService service;

    @BeforeEach
    void setUp() {
        service = new DeploymentPreCheckService();
    }

    @Test
    void runAll_withNoChecks_returnsEmptySuitePass() {
        PreCheckSuiteResult result = service.runAll("staging", "1.0.0");
        assertTrue(result.isAllPassed());
        assertTrue(result.getResults().isEmpty());
        assertFalse(result.hasBlockingFailure());
    }

    @Test
    void runAll_withPassingCheck_returnsPassed() {
        service.register("disk-space", PreCheckSeverity.CRITICAL, () ->
                PreCheckResult.builder("disk-space").passed(true).message("OK").build());

        PreCheckSuiteResult result = service.runAll("prod", "2.1.0");
        assertTrue(result.isAllPassed());
        assertFalse(result.hasBlockingFailure());
        assertEquals(1, result.getResults().size());
    }

    @Test
    void runAll_withCriticalFailure_marksBlockingFailure() {
        service.register("config-check", PreCheckSeverity.CRITICAL, () ->
                PreCheckResult.builder("config-check")
                        .passed(false)
                        .severity(PreCheckSeverity.CRITICAL)
                        .message("Missing required config")
                        .build());

        PreCheckSuiteResult result = service.runAll("prod", "2.1.0");
        assertFalse(result.isAllPassed());
        assertTrue(result.hasBlockingFailure());
    }

    @Test
    void runAll_withWarningFailure_doesNotBlockDeployment() {
        service.register("optional-check", PreCheckSeverity.WARNING, () ->
                PreCheckResult.builder("optional-check")
                        .passed(false)
                        .severity(PreCheckSeverity.WARNING)
                        .message("Non-critical issue")
                        .build());

        PreCheckSuiteResult result = service.runAll("staging", "1.5.0");
        assertFalse(result.isAllPassed());
        assertFalse(result.hasBlockingFailure());
    }

    @Test
    void runAll_whenCheckThrowsException_capturesFailure() {
        service.register("broken-check", PreCheckSeverity.CRITICAL, () -> {
            throw new RuntimeException("Unexpected error");
        });

        PreCheckSuiteResult result = service.runAll("dev", "1.0.0");
        assertFalse(result.isAllPassed());
        assertTrue(result.hasBlockingFailure());
        assertTrue(result.getResults().get(0).getMessages().get(0).contains("Unexpected error"));
    }

    @Test
    void getCheckCount_returnsCorrectCount() {
        assertEquals(0, service.getCheckCount());
        service.register("check-1", PreCheckSeverity.WARNING, () ->
                PreCheckResult.builder("check-1").passed(true).build());
        service.register("check-2", PreCheckSeverity.CRITICAL, () ->
                PreCheckResult.builder("check-2").passed(true).build());
        assertEquals(2, service.getCheckCount());
    }

    @Test
    void clear_removesAllChecks() {
        service.register("check-1", PreCheckSeverity.WARNING, () ->
                PreCheckResult.builder("check-1").passed(true).build());
        service.clear();
        assertEquals(0, service.getCheckCount());
    }
}
