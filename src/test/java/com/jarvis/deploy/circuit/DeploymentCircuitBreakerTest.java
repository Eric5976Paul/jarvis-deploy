package com.jarvis.deploy.circuit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.*;

class DeploymentCircuitBreakerTest {

    private DeploymentCircuitBreaker breaker;

    @BeforeEach
    void setUp() {
        breaker = new DeploymentCircuitBreaker("staging", 3, Duration.ofSeconds(30));
    }

    @Test
    void initialStateIsClosed() {
        assertThat(breaker.getState()).isEqualTo(CircuitBreakerState.CLOSED);
        assertThat(breaker.allowDeployment()).isTrue();
    }

    @Test
    void opensAfterThresholdFailures() {
        breaker.recordFailure();
        breaker.recordFailure();
        assertThat(breaker.getState()).isEqualTo(CircuitBreakerState.CLOSED);
        breaker.recordFailure();
        assertThat(breaker.getState()).isEqualTo(CircuitBreakerState.OPEN);
        assertThat(breaker.allowDeployment()).isFalse();
    }

    @Test
    void successResetsToClosedState() {
        breaker.recordFailure();
        breaker.recordFailure();
        breaker.recordFailure();
        assertThat(breaker.getState()).isEqualTo(CircuitBreakerState.OPEN);
        breaker.recordSuccess();
        assertThat(breaker.getState()).isEqualTo(CircuitBreakerState.CLOSED);
        assertThat(breaker.getFailureCount()).isZero();
    }

    @Test
    void transitionsToHalfOpenAfterOpenDurationExpires() throws InterruptedException {
        DeploymentCircuitBreaker fastBreaker =
                new DeploymentCircuitBreaker("prod", 1, Duration.ofMillis(50));
        fastBreaker.recordFailure();
        assertThat(fastBreaker.getState()).isEqualTo(CircuitBreakerState.OPEN);
        Thread.sleep(100);
        assertThat(fastBreaker.getState()).isEqualTo(CircuitBreakerState.HALF_OPEN);
        assertThat(fastBreaker.allowDeployment()).isTrue();
    }

    @Test
    void reopensFromHalfOpenOnFailure() throws InterruptedException {
        DeploymentCircuitBreaker fastBreaker =
                new DeploymentCircuitBreaker("dev", 1, Duration.ofMillis(50));
        fastBreaker.recordFailure();
        Thread.sleep(100);
        assertThat(fastBreaker.getState()).isEqualTo(CircuitBreakerState.HALF_OPEN);
        fastBreaker.recordFailure();
        assertThat(fastBreaker.getState()).isEqualTo(CircuitBreakerState.OPEN);
    }

    @Test
    void resetClearsAllCountersAndState() {
        breaker.recordFailure();
        breaker.recordFailure();
        breaker.recordFailure();
        breaker.reset();
        assertThat(breaker.getState()).isEqualTo(CircuitBreakerState.CLOSED);
        assertThat(breaker.getFailureCount()).isZero();
        assertThat(breaker.allowDeployment()).isTrue();
    }

    @Test
    void throwsOnBlankEnvironment() {
        assertThatThrownBy(() -> new DeploymentCircuitBreaker("", 3, Duration.ofMinutes(1)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void throwsOnZeroThreshold() {
        assertThatThrownBy(() -> new DeploymentCircuitBreaker("staging", 0, Duration.ofMinutes(1)))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
