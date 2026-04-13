package com.jarvis.deploy.canary;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CanaryEvaluatorTest {

    private CanaryEvaluator evaluator;
    private CanaryDeploymentConfig config;

    @BeforeEach
    void setUp() {
        evaluator = new CanaryEvaluator(0.05, 300.0);
        config = new CanaryDeploymentConfig("staging", "my-app",
                "2.1.0", "2.0.0", 10, 300);
    }

    @Test
    void evaluate_shouldPromote_whenMetricsWithinThresholds() {
        CanaryEvaluationResult result = evaluator.evaluate(config, 0.01, 150.0);

        assertEquals(CanaryEvaluationResult.Decision.PROMOTE, result.getDecision());
        assertTrue(result.isPromote());
        assertFalse(result.isRollback());
        assertEquals(0.01, result.getErrorRate());
        assertEquals(150.0, result.getP99LatencyMs());
    }

    @Test
    void evaluate_shouldRollback_whenErrorRateExceedsThreshold() {
        CanaryEvaluationResult result = evaluator.evaluate(config, 0.10, 100.0);

        assertEquals(CanaryEvaluationResult.Decision.ROLLBACK, result.getDecision());
        assertTrue(result.isRollback());
        assertTrue(result.getReason().contains("Error rate"));
    }

    @Test
    void evaluate_shouldRollback_whenLatencyExceedsThreshold() {
        CanaryEvaluationResult result = evaluator.evaluate(config, 0.01, 500.0);

        assertEquals(CanaryEvaluationResult.Decision.ROLLBACK, result.getDecision());
        assertTrue(result.getReason().contains("P99 latency"));
    }

    @Test
    void evaluate_shouldBeInconclusive_whenErrorRateIsInvalid() {
        CanaryEvaluationResult result = evaluator.evaluate(config, -0.5, 100.0);

        assertEquals(CanaryEvaluationResult.Decision.INCONCLUSIVE, result.getDecision());
        assertFalse(result.isPromote());
        assertFalse(result.isRollback());
    }

    @Test
    void evaluate_shouldPromote_whenMetricsAreExactlyAtThreshold() {
        CanaryEvaluationResult result = evaluator.evaluate(config, 0.05, 300.0);

        assertEquals(CanaryEvaluationResult.Decision.PROMOTE, result.getDecision());
    }

    @Test
    void canaryDeploymentConfig_shouldRejectInvalidTrafficPercent() {
        assertThrows(IllegalArgumentException.class, () ->
                new CanaryDeploymentConfig("prod", "app", "1.1", "1.0", 110, 60));
        assertThrows(IllegalArgumentException.class, () ->
                new CanaryDeploymentConfig("prod", "app", "1.1", "1.0", -5, 60));
    }

    @Test
    void canaryEvaluationResult_toStringShouldContainDecision() {
        CanaryEvaluationResult result = evaluator.evaluate(config, 0.02, 200.0);
        String str = result.toString();
        assertTrue(str.contains("PROMOTE"));
        assertTrue(str.contains("p99"));
    }
}
