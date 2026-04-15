package com.jarvis.deploy.summary;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class DeploymentSummaryReportTest {

    private static final Instant FROM = Instant.parse("2024-06-01T00:00:00Z");
    private static final Instant TO   = Instant.parse("2024-06-30T23:59:59Z");

    @Test
    void shouldBuildReportWithAllFields() {
        DeploymentSummaryReport report = DeploymentSummaryReport
                .builder("production", FROM, TO)
                .totalDeployments(20)
                .successfulDeployments(16)
                .failedDeployments(3)
                .rolledBackDeployments(1)
                .averageDurationMs(45_000L)
                .deploymentsByService(Map.of("order-service", 8, "user-service", 12))
                .topFailureReasons(List.of("health check timeout", "artifact not found"))
                .build();

        assertEquals("production", report.getEnvironment());
        assertEquals(FROM, report.getFrom());
        assertEquals(TO, report.getTo());
        assertEquals(20, report.getTotalDeployments());
        assertEquals(16, report.getSuccessfulDeployments());
        assertEquals(3, report.getFailedDeployments());
        assertEquals(1, report.getRolledBackDeployments());
        assertEquals(45_000L, report.getAverageDurationMs());
        assertEquals(2, report.getDeploymentsByService().size());
        assertEquals(2, report.getTopFailureReasons().size());
    }

    @Test
    void shouldCalculateSuccessRateCorrectly() {
        DeploymentSummaryReport report = DeploymentSummaryReport
                .builder("staging", FROM, TO)
                .totalDeployments(10)
                .successfulDeployments(8)
                .build();

        assertEquals(80.0, report.getSuccessRate(), 0.001);
    }

    @Test
    void shouldReturnZeroSuccessRateWhenNoDeployments() {
        DeploymentSummaryReport report = DeploymentSummaryReport
                .builder("dev", FROM, TO)
                .totalDeployments(0)
                .successfulDeployments(0)
                .build();

        assertEquals(0.0, report.getSuccessRate(), 0.001);
    }

    @Test
    void shouldReturnImmutableCollections() {
        DeploymentSummaryReport report = DeploymentSummaryReport
                .builder("production", FROM, TO)
                .deploymentsByService(Map.of("svc-a", 5))
                .topFailureReasons(List.of("OOM"))
                .build();

        assertThrows(UnsupportedOperationException.class,
                () -> report.getDeploymentsByService().put("svc-b", 3));
        assertThrows(UnsupportedOperationException.class,
                () -> report.getTopFailureReasons().add("disk full"));
    }

    @Test
    void shouldThrowWhenRequiredFieldsMissing() {
        assertThrows(NullPointerException.class,
                () -> DeploymentSummaryReport.builder(null, FROM, TO).build());
        assertThrows(NullPointerException.class,
                () -> DeploymentSummaryReport.builder("prod", null, TO).build());
        assertThrows(NullPointerException.class,
                () -> DeploymentSummaryReport.builder("prod", FROM, null).build());
    }

    @Test
    void shouldDefaultToEmptyCollectionsWhenNotSet() {
        DeploymentSummaryReport report = DeploymentSummaryReport
                .builder("dev", FROM, TO)
                .build();

        assertTrue(report.getDeploymentsByService().isEmpty());
        assertTrue(report.getTopFailureReasons().isEmpty());
    }
}
