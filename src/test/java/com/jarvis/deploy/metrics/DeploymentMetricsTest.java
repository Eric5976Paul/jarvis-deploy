package com.jarvis.deploy.metrics;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

class DeploymentMetricsTest {

    private DeploymentMetrics metrics;

    @BeforeEach
    void setUp() {
        metrics = new DeploymentMetrics();
    }

    @Test
    void recordSuccess_incrementsSuccessCount() {
        metrics.recordSuccess("prod", Duration.ofSeconds(5));
        metrics.recordSuccess("prod", Duration.ofSeconds(3));
        assertEquals(2, metrics.getSuccessCount("prod"));
    }

    @Test
    void recordFailure_incrementsFailureCount() {
        metrics.recordFailure("staging", Duration.ofSeconds(2));
        assertEquals(1, metrics.getFailureCount("staging"));
    }

    @Test
    void getAverageDurationMs_calculatesCorrectly() {
        metrics.recordSuccess("prod", Duration.ofMillis(1000));
        metrics.recordSuccess("prod", Duration.ofMillis(3000));
        assertEquals(2000.0, metrics.getAverageDurationMs("prod"), 0.001);
    }

    @Test
    void getAverageDurationMs_returnsZeroWhenNoRecords() {
        assertEquals(0.0, metrics.getAverageDurationMs("dev"), 0.001);
    }

    @Test
    void getSummary_returnsCorrectSummary() {
        metrics.recordSuccess("prod", Duration.ofMillis(2000));
        metrics.recordFailure("prod", Duration.ofMillis(2000));

        MetricsSummary summary = metrics.getSummary("prod");

        assertEquals("prod", summary.getEnvironment());
        assertEquals(1, summary.getSuccessCount());
        assertEquals(1, summary.getFailureCount());
        assertEquals(2, summary.getTotalCount());
        assertEquals(50.0, summary.getSuccessRate(), 0.001);
        assertEquals(2000.0, summary.getAverageDurationMs(), 0.001);
    }

    @Test
    void reset_clearsAllMetricsForEnvironment() {
        metrics.recordSuccess("prod", Duration.ofSeconds(1));
        metrics.recordFailure("prod", Duration.ofSeconds(1));
        metrics.reset("prod");

        assertEquals(0, metrics.getSuccessCount("prod"));
        assertEquals(0, metrics.getFailureCount("prod"));
        assertEquals(0.0, metrics.getAverageDurationMs("prod"), 0.001);
    }

    @Test
    void recordSuccess_throwsOnBlankEnvironment() {
        assertThrows(IllegalArgumentException.class,
            () -> metrics.recordSuccess(" ", Duration.ofSeconds(1)));
    }

    @Test
    void recordFailure_throwsOnNullEnvironment() {
        assertThrows(IllegalArgumentException.class,
            () -> metrics.recordFailure(null, Duration.ofSeconds(1)));
    }

    @Test
    void metrics_areIsolatedPerEnvironment() {
        metrics.recordSuccess("prod", Duration.ofSeconds(1));
        metrics.recordSuccess("staging", Duration.ofSeconds(1));
        metrics.recordSuccess("staging", Duration.ofSeconds(1));

        assertEquals(1, metrics.getSuccessCount("prod"));
        assertEquals(2, metrics.getSuccessCount("staging"));
    }
}
