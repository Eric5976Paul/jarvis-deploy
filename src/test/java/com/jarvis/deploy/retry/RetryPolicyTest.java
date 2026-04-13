package com.jarvis.deploy.retry;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

class RetryPolicyTest {

    @Test
    void defaultPolicy_shouldHaveThreeAttempts() {
        RetryPolicy policy = RetryPolicy.defaultPolicy();
        assertEquals(3, policy.getMaxAttempts());
    }

    @Test
    void noRetry_shouldNotRetryAfterFirstAttempt() {
        RetryPolicy policy = RetryPolicy.noRetry();
        assertEquals(1, policy.getMaxAttempts());
        assertFalse(policy.shouldRetry(1));
    }

    @Test
    void shouldRetry_returnsTrueWhenBelowMaxAttempts() {
        RetryPolicy policy = RetryPolicy.defaultPolicy();
        assertTrue(policy.shouldRetry(1));
        assertTrue(policy.shouldRetry(2));
        assertFalse(policy.shouldRetry(3));
    }

    @Test
    void getDelayForAttempt_firstAttemptUsesInitialDelay() {
        RetryPolicy policy = new RetryPolicy(3, Duration.ofSeconds(2), 2.0, Duration.ofSeconds(60));
        assertEquals(Duration.ofSeconds(2), policy.getDelayForAttempt(1));
    }

    @Test
    void getDelayForAttempt_secondAttemptAppliesBackoff() {
        RetryPolicy policy = new RetryPolicy(3, Duration.ofSeconds(2), 2.0, Duration.ofSeconds(60));
        assertEquals(Duration.ofSeconds(4), policy.getDelayForAttempt(2));
    }

    @Test
    void getDelayForAttempt_respectsMaxDelayCap() {
        RetryPolicy policy = new RetryPolicy(5, Duration.ofSeconds(10), 3.0, Duration.ofSeconds(20));
        Duration delay = policy.getDelayForAttempt(4);
        assertTrue(delay.compareTo(Duration.ofSeconds(20)) <= 0);
    }

    @Test
    void constructor_throwsOnInvalidMaxAttempts() {
        assertThrows(IllegalArgumentException.class,
                () -> new RetryPolicy(0, Duration.ofSeconds(1), 1.0, Duration.ofSeconds(10)));
    }

    @Test
    void constructor_throwsOnInvalidBackoffMultiplier() {
        assertThrows(IllegalArgumentException.class,
                () -> new RetryPolicy(3, Duration.ofSeconds(1), 0.5, Duration.ofSeconds(10)));
    }

    @Test
    void toString_containsKeyFields() {
        RetryPolicy policy = RetryPolicy.defaultPolicy();
        String str = policy.toString();
        assertTrue(str.contains("maxAttempts=3"));
        assertTrue(str.contains("backoffMultiplier=2.0"));
    }
}
