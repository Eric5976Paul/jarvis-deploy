package com.jarvis.deploy.compliancy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static com.jarvis.deploy.compliancy.ComplianceRule.ComplianceSeverity.*;
import static org.junit.jupiter.api.Assertions.*;

class ComplianceCheckServiceTest {

    private ComplianceCheckService service;

    @BeforeEach
    void setUp() {
        service = new ComplianceCheckService();
    }

    @Test
    void noRules_returnsPassedResult() {
        var result = service.check("production", Map.of());
        assertTrue(result.isPassed());
        assertFalse(result.isBlocked());
        assertEquals(0, result.violationCount());
    }

    @Test
    void passingRule_producesNoViolation() {
        service.register(
                new ComplianceRule("R001", "Version must be set", BLOCKING, null),
                ctx -> ctx.containsKey("version")
        );
        var result = service.check("staging", Map.of("version", "1.2.3"));
        assertTrue(result.isPassed());
        assertEquals(0, result.violationCount());
    }

    @Test
    void blockingViolation_blocksDeployment() {
        service.register(
                new ComplianceRule("R002", "Requester must be set", BLOCKING, null),
                ctx -> ctx.containsKey("requester")
        );
        var result = service.check("production", Map.of("version", "1.0.0"));
        assertFalse(result.isPassed());
        assertTrue(result.isBlocked());
        assertEquals(1, result.violationCount());
        assertEquals("R002", result.getViolations().get(0).getRule().getRuleId());
    }

    @Test
    void warningViolation_doesNotBlock() {
        service.register(
                new ComplianceRule("R003", "Change ticket recommended", WARNING, null),
                ctx -> ctx.containsKey("changeTicket")
        );
        var result = service.check("production", Map.of("version", "2.0.0"));
        assertTrue(result.isPassed());   // not blocked
        assertFalse(result.isBlocked());
        assertEquals(1, result.violationCount()); // but violation recorded
    }

    @Test
    void ruleFilteredByEnvironment_doesNotApplyToOtherEnv() {
        service.register(
                new ComplianceRule("R004", "Prod-only approval required", BLOCKING, "production"),
                ctx -> ctx.containsKey("approvalId")
        );
        // Should not block staging even though approvalId is missing
        var stagingResult = service.check("staging", Map.of("version", "1.0.0"));
        assertTrue(stagingResult.isPassed());

        // Should block production
        var prodResult = service.check("production", Map.of("version", "1.0.0"));
        assertTrue(prodResult.isBlocked());
    }

    @Test
    void multipleRules_allViolationsCollected() {
        service.register(new ComplianceRule("R005", "Version required", BLOCKING, null),
                ctx -> ctx.containsKey("version"));
        service.register(new ComplianceRule("R006", "Requester required", BLOCKING, null),
                ctx -> ctx.containsKey("requester"));

        var result = service.check("production", Map.of());
        assertTrue(result.isBlocked());
        assertEquals(2, result.violationCount());
    }

    @Test
    void listRules_returnsAllRegistered() {
        service.register(new ComplianceRule("R007", "desc", INFO, null), ctx -> true);
        service.register(new ComplianceRule("R008", "desc2", WARNING, "staging"), ctx -> true);
        assertEquals(2, service.listRules().size());
    }
}
