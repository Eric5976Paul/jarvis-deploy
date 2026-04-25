package com.jarvis.deploy.watchdog;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class DeploymentWatchdogTest {

    private DeploymentWatchdog watchdog;

    @BeforeEach
    void setUp() {
        watchdog = new DeploymentWatchdog(Duration.ofMinutes(10));
    }

    @Test
    void shouldWatchDeploymentWithDefaultThreshold() {
        watchdog.watch("deploy-001", "staging");
        assertTrue(watchdog.isWatched("deploy-001"));
        assertEquals(1, watchdog.watchedCount());
    }

    @Test
    void shouldWatchDeploymentWithCustomThreshold() {
        watchdog.watch("deploy-002", "production", Duration.ofMinutes(30));
        assertTrue(watchdog.isWatched("deploy-002"));
    }

    @Test
    void shouldUnwatchDeployment() {
        watchdog.watch("deploy-003", "dev");
        watchdog.unwatch("deploy-003");
        assertFalse(watchdog.isWatched("deploy-003"));
        assertEquals(0, watchdog.watchedCount());
    }

    @Test
    void shouldReturnHealthyForRecentDeployment() {
        watchdog.watch("deploy-004", "staging");
        WatchdogCheckResult result = watchdog.check("deploy-004");
        assertEquals(WatchdogCheckResult.Status.HEALTHY, result.getStatus());
        assertTrue(result.isHealthy());
        assertFalse(result.isStalled());
        assertEquals("staging", result.getEnvironment());
    }

    @Test
    void shouldReturnNotWatchedForUnknownDeployment() {
        WatchdogCheckResult result = watchdog.check("unknown-deploy");
        assertEquals(WatchdogCheckResult.Status.NOT_WATCHED, result.getStatus());
        assertNull(result.getElapsed());
    }

    @Test
    void shouldDetectStalledDeploymentWithVeryShortThreshold() throws InterruptedException {
        watchdog.watch("deploy-005", "production", Duration.ofMillis(1));
        Thread.sleep(10);
        WatchdogCheckResult result = watchdog.check("deploy-005");
        assertEquals(WatchdogCheckResult.Status.STALLED, result.getStatus());
        assertTrue(result.isStalled());
        assertNotNull(result.getElapsed());
    }

    @Test
    void shouldCheckAllWatchedDeployments() {
        watchdog.watch("deploy-006", "staging");
        watchdog.watch("deploy-007", "production");
        Map<String, WatchdogCheckResult> results = watchdog.checkAll();
        assertEquals(2, results.size());
        assertTrue(results.containsKey("deploy-006"));
        assertTrue(results.containsKey("deploy-007"));
    }

    @Test
    void shouldRejectNullOrBlankDeploymentId() {
        assertThrows(IllegalArgumentException.class, () -> watchdog.watch(null, "staging"));
        assertThrows(IllegalArgumentException.class, () -> watchdog.watch("  ", "staging"));
    }

    @Test
    void shouldRejectNegativeStallThreshold() {
        assertThrows(IllegalArgumentException.class,
                () -> new DeploymentWatchdog(Duration.ofSeconds(-1)));
    }

    @Test
    void shouldIncludeMessageInResult() {
        watchdog.watch("deploy-008", "dev");
        WatchdogCheckResult result = watchdog.check("deploy-008");
        assertNotNull(result.getMessage());
        assertFalse(result.getMessage().isBlank());
    }
}
