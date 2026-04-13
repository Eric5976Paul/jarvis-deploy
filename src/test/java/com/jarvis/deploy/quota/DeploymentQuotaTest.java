package com.jarvis.deploy.quota;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DeploymentQuotaTest {

    private DeploymentQuota quota;

    @BeforeEach
    void setUp() {
        quota = new DeploymentQuota("staging", 3);
    }

    @Test
    void shouldAllowDeploymentWithinQuota() {
        QuotaCheckResult result = quota.tryConsume();
        assertTrue(result.isAllowed());
        assertEquals("staging", result.getEnvironment());
        assertEquals(1, result.getCurrentCount());
        assertEquals(3, result.getMaxAllowed());
    }

    @Test
    void shouldAllowUpToMaxDeployments() {
        quota.tryConsume();
        quota.tryConsume();
        QuotaCheckResult third = quota.tryConsume();
        assertTrue(third.isAllowed());
        assertEquals(3, third.getCurrentCount());
    }

    @Test
    void shouldDenyDeploymentWhenQuotaExceeded() {
        quota.tryConsume();
        quota.tryConsume();
        quota.tryConsume();
        QuotaCheckResult result = quota.tryConsume();
        assertTrue(result.isDenied());
        assertFalse(result.isAllowed());
        assertEquals(3, result.getCurrentCount());
    }

    @Test
    void shouldNotIncrementCountOnDeniedRequest() {
        quota.tryConsume();
        quota.tryConsume();
        quota.tryConsume();
        quota.tryConsume(); // denied
        assertEquals(3, quota.currentUsage());
    }

    @Test
    void shouldReturnCurrentUsageWithoutConsuming() {
        quota.tryConsume();
        quota.tryConsume();
        assertEquals(2, quota.currentUsage());
        assertEquals(2, quota.currentUsage()); // no side effect
    }

    @Test
    void shouldThrowOnBlankEnvironment() {
        assertThrows(IllegalArgumentException.class, () -> new DeploymentQuota("", 5));
        assertThrows(IllegalArgumentException.class, () -> new DeploymentQuota(null, 5));
    }

    @Test
    void shouldThrowOnNonPositiveLimit() {
        assertThrows(IllegalArgumentException.class, () -> new DeploymentQuota("prod", 0));
        assertThrows(IllegalArgumentException.class, () -> new DeploymentQuota("prod", -1));
    }

    @Test
    void shouldIncludeDenialMessageOnExceeded() {
        quota.tryConsume();
        quota.tryConsume();
        quota.tryConsume();
        QuotaCheckResult result = quota.tryConsume();
        assertNotNull(result.getMessage());
        assertTrue(result.getMessage().contains("staging"));
        assertTrue(result.getMessage().contains("exceeded") || result.getMessage().contains("Quota"));
    }

    @Test
    void shouldExposeWindowStart() {
        assertNotNull(quota.getWindowStart());
    }
}
