package com.jarvis.deploy.gate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DeploymentGateServiceTest {

    private DeploymentGateService service;

    @BeforeEach
    void setUp() {
        service = new DeploymentGateService();
    }

    @Test
    void registerAndRetrieveGate() {
        DeploymentGate gate = new DeploymentGate("g1", "Smoke Tests", "staging", List.of("smoke-pass"));
        service.registerGate(gate);

        List<DeploymentGate> gates = service.getGatesForEnvironment("staging");
        assertEquals(1, gates.size());
        assertEquals("g1", gates.get(0).getGateId());
    }

    @Test
    void evaluateGates_allPending_autoPassesAndReturnsSuccess() {
        service.registerGate(new DeploymentGate("g1", "Unit Tests", "dev", List.of("unit-pass")));
        service.registerGate(new DeploymentGate("g2", "Lint", "dev", List.of("lint-pass")));

        GateEvaluationResult result = service.evaluateGates("dev", "ci-bot");

        assertTrue(result.isAllPassed());
        assertEquals(2, result.countPassed());
        assertEquals(0, result.countFailed());
        assertTrue(result.getFailureReasons().isEmpty());
    }

    @Test
    void evaluateGates_withFailedGate_returnsFailure() {
        DeploymentGate gate = new DeploymentGate("g3", "Security Scan", "prod", List.of("sec-scan"));
        gate.fail("security-bot");
        service.registerGate(gate);

        GateEvaluationResult result = service.evaluateGates("prod", "ci-bot");

        assertFalse(result.isAllPassed());
        assertEquals(1, result.countFailed());
        assertFalse(result.getFailureReasons().isEmpty());
        assertTrue(result.getFailureReasons().get(0).contains("Security Scan"));
    }

    @Test
    void evaluateGates_noGatesRegistered_returnsSuccess() {
        GateEvaluationResult result = service.evaluateGates("staging", "user");
        assertTrue(result.isAllPassed());
        assertTrue(result.getGates().isEmpty());
    }

    @Test
    void clearGates_removesAllGatesForEnvironment() {
        service.registerGate(new DeploymentGate("g1", "Gate A", "qa", List.of("cond-a")));
        service.clearGates("qa");

        assertTrue(service.getGatesForEnvironment("qa").isEmpty());
    }

    @Test
    void totalGateCount_reflectsAllEnvironments() {
        service.registerGate(new DeploymentGate("g1", "G1", "dev", List.of()));
        service.registerGate(new DeploymentGate("g2", "G2", "staging", List.of()));
        service.registerGate(new DeploymentGate("g3", "G3", "prod", List.of()));

        assertEquals(3, service.totalGateCount());
    }

    @Test
    void gateStatus_passedGate_isCorrect() {
        DeploymentGate gate = new DeploymentGate("g1", "Gate", "dev", List.of("cond"));
        gate.pass("user");

        assertTrue(gate.isPassed());
        assertFalse(gate.isFailed());
        assertFalse(gate.isPending());
        assertEquals("user", gate.getEvaluatedBy());
        assertNotNull(gate.getEvaluatedAt());
    }
}
