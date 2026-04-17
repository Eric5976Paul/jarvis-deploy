package com.jarvis.deploy.alerting;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DeploymentAlertServiceTest {
    private DeploymentAlertService service;

    @BeforeEach
    void setUp() {
        service = new DeploymentAlertService();
    }

    @Test
    void noRules_returnsEmptyAlerts() {
        List<DeploymentAlert> alerts = service.evaluate("prod", "deployment failed");
        assertTrue(alerts.isEmpty());
    }

    @Test
    void matchingRule_triggersAlert() {
        AlertRule rule = new AlertRule("fail-rule", AlertSeverity.CRITICAL,
            ctx -> ctx.contains("failed"), "Deployment issue: {context}");
        service.registerRule(rule);

        List<DeploymentAlert> alerts = service.evaluate("prod", "deployment failed");
        assertEquals(1, alerts.size());
        assertEquals(AlertSeverity.CRITICAL, alerts.get(0).getSeverity());
        assertEquals("prod", alerts.get(0).getEnvironment());
        assertTrue(alerts.get(0).getMessage().contains("deployment failed"));
    }

    @Test
    void nonMatchingRule_noAlert() {
        AlertRule rule = new AlertRule("warn-rule", AlertSeverity.WARNING,
            ctx -> ctx.contains("timeout"), "Timeout: {context}");
        service.registerRule(rule);

        List<DeploymentAlert> alerts = service.evaluate("staging", "deployment succeeded");
        assertTrue(alerts.isEmpty());
    }

    @Test
    void historyAccumulates() {
        AlertRule rule = new AlertRule("info-rule", AlertSeverity.INFO,
            ctx -> true, "Event: {context}");
        service.registerRule(rule);

        service.evaluate("dev", "start");
        service.evaluate("prod", "finish");

        assertEquals(2, service.getHistory().size());
    }

    @Test
    void filterByEnvironment() {
        AlertRule rule = new AlertRule("r", AlertSeverity.INFO, ctx -> true, "{context}");
        service.registerRule(rule);

        service.evaluate("dev", "a");
        service.evaluate("prod", "b");

        assertEquals(1, service.getHistoryByEnvironment("dev").size());
        assertEquals(1, service.getHistoryByEnvironment("prod").size());
    }

    @Test
    void clearHistory_removesAll() {
        AlertRule rule = new AlertRule("r", AlertSeverity.INFO, ctx -> true, "{context}");
        service.registerRule(rule);
        service.evaluate("dev", "x");
        service.clearHistory();
        assertTrue(service.getHistory().isEmpty());
    }

    @Test
    void nullRule_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> service.registerRule(null));
    }
}
