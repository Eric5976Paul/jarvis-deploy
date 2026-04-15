package com.jarvis.deploy.concurrency;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ConcurrencyGuardTest {

    private ConcurrencyGuard guard;

    @BeforeEach
    void setUp() {
        guard = new ConcurrencyGuard();
    }

    @Test
    void shouldAcquireSlotWhenUnderLimit() {
        guard.registerPolicy(new ConcurrencyPolicy("staging", 2, ConcurrencyViolationAction.REJECT, 0));
        ConcurrencyAcquireResult result = guard.acquire("staging");
        assertTrue(result.isAcquired());
        assertEquals("staging", result.getEnvironment());
    }

    @Test
    void shouldRejectWhenLimitReachedWithRejectPolicy() {
        guard.registerPolicy(new ConcurrencyPolicy("prod", 1, ConcurrencyViolationAction.REJECT, 0));
        ConcurrencyAcquireResult first = guard.acquire("prod");
        assertTrue(first.isAcquired());

        ConcurrencyAcquireResult second = guard.acquire("prod");
        assertFalse(second.isAcquired());
        assertTrue(second.getMessage().contains("Concurrency limit reached"));
    }

    @Test
    void shouldReleaseAndAllowSubsequentAcquire() {
        guard.registerPolicy(new ConcurrencyPolicy("dev", 1, ConcurrencyViolationAction.REJECT, 0));
        guard.acquire("dev");
        guard.release("dev");

        ConcurrencyAcquireResult result = guard.acquire("dev");
        assertTrue(result.isAcquired());
    }

    @Test
    void shouldUseDefaultPolicyForUnregisteredEnvironment() {
        ConcurrencyAcquireResult first = guard.acquire("unknown-env");
        assertTrue(first.isAcquired());

        ConcurrencyAcquireResult second = guard.acquire("unknown-env");
        assertFalse(second.isAcquired());
    }

    @Test
    void shouldReportAvailableSlots() {
        guard.registerPolicy(new ConcurrencyPolicy("qa", 3, ConcurrencyViolationAction.REJECT, 0));
        assertEquals(3, guard.availableSlots("qa"));
        guard.acquire("qa");
        assertEquals(2, guard.availableSlots("qa"));
    }

    @Test
    void shouldReturnNegativeOneForUnknownEnvironmentSlots() {
        assertEquals(-1, guard.availableSlots("nonexistent"));
    }

    @Test
    void shouldRejectAfterQueueTimeout() {
        guard.registerPolicy(new ConcurrencyPolicy("perf", 1, ConcurrencyViolationAction.QUEUE, 1));
        guard.acquire("perf"); // fill the slot

        long start = System.currentTimeMillis();
        ConcurrencyAcquireResult result = guard.acquire("perf");
        long elapsed = System.currentTimeMillis() - start;

        assertFalse(result.isAcquired());
        assertTrue(elapsed >= 1000, "Should have waited at least 1 second");
    }

    @Test
    void shouldThrowForInvalidMaxConcurrent() {
        assertThrows(IllegalArgumentException.class, () ->
                new ConcurrencyPolicy("env", 0, ConcurrencyViolationAction.REJECT, 0));
    }

    @Test
    void shouldThrowForBlankEnvironment() {
        assertThrows(IllegalArgumentException.class, () ->
                new ConcurrencyPolicy(" ", 1, ConcurrencyViolationAction.REJECT, 0));
    }
}
