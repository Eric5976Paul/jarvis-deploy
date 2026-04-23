package com.jarvis.deploy.ratelimit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DeploymentRateLimiterTest {

    private DeploymentRateLimiter rateLimiter;

    @BeforeEach
    void setUp() {
        rateLimiter = new DeploymentRateLimiter(3, 60);
    }

    @Test
    void allowsDeploymentWithinLimit() {
        RateLimitResult result = rateLimiter.tryAcquire("staging");
        assertTrue(result.isAllowed());
        assertEquals("staging", result.getEnvironment());
        assertEquals(1, result.getCurrentCount());
        assertEquals(3, result.getMaxAllowed());
        assertEquals(2, result.getRemainingSlots());
    }

    @Test
    void tracksMultipleDeploymentsPerEnvironment() {
        rateLimiter.tryAcquire("prod");
        rateLimiter.tryAcquire("prod");
        RateLimitResult third = rateLimiter.tryAcquire("prod");
        assertTrue(third.isAllowed());
        assertEquals(3, third.getCurrentCount());
        assertEquals(0, third.getRemainingSlots());
    }

    @Test
    void deniesBeyondLimit() {
        rateLimiter.tryAcquire("prod");
        rateLimiter.tryAcquire("prod");
        rateLimiter.tryAcquire("prod");
        RateLimitResult denied = rateLimiter.tryAcquire("prod");
        assertFalse(denied.isAllowed());
        assertNotNull(denied.getReason());
        assertTrue(denied.getReason().contains("Rate limit exceeded"));
    }

    @Test
    void isolatesCountsPerEnvironment() {
        rateLimiter.tryAcquire("staging");
        rateLimiter.tryAcquire("staging");
        rateLimiter.tryAcquire("staging");
        RateLimitResult prodResult = rateLimiter.tryAcquire("prod");
        assertTrue(prodResult.isAllowed(), "prod should not be affected by staging limit");
    }

    @Test
    void resetClearsCountForEnvironment() {
        rateLimiter.tryAcquire("qa");
        rateLimiter.tryAcquire("qa");
        rateLimiter.tryAcquire("qa");
        rateLimiter.reset("qa");
        RateLimitResult result = rateLimiter.tryAcquire("qa");
        assertTrue(result.isAllowed());
        assertEquals(1, result.getCurrentCount());
    }

    @Test
    void currentCountReturnsZeroForUnknownEnvironment() {
        assertEquals(0, rateLimiter.currentCount("unknown-env"));
    }

    @Test
    void rejectsBlankEnvironment() {
        RateLimitResult result = rateLimiter.tryAcquire("  ");
        assertFalse(result.isAllowed());
        assertNotNull(result.getReason());
    }

    @Test
    void constructorRejectsInvalidParams() {
        assertThrows(IllegalArgumentException.class, () -> new DeploymentRateLimiter(0, 60));
        assertThrows(IllegalArgumentException.class, () -> new DeploymentRateLimiter(5, 0));
    }

    @Test
    void toStringContainsRelevantInfo() {
        RateLimitResult allowed = RateLimitResult.allowed("dev", 1, 5);
        assertTrue(allowed.toString().contains("ALLOWED"));
        assertTrue(allowed.toString().contains("dev"));

        RateLimitResult denied = RateLimitResult.denied("dev", 5, 5, "Rate limit exceeded. Retry after 10s");
        assertTrue(denied.toString().contains("DENIED"));
        assertTrue(denied.toString().contains("Retry after"));
    }
}
