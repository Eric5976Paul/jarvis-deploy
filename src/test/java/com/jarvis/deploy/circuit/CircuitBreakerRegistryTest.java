package com.jarvis.deploy.circuit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

class CircuitBreakerRegistryTest {

    private CircuitBreakerRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new CircuitBreakerRegistry(2, Duration.ofMinutes(10));
    }

    @Test
    void getOrCreateReturnsSameBreakerForSameEnvironment() {
        DeploymentCircuitBreaker first = registry.getOrCreate("staging");
        DeploymentCircuitBreaker second = registry.getOrCreate("staging");
        assertThat(first).isSameAs(second);
    }

    @Test
    void getOrCreateCreatesDistinctBreakersPerEnvironment() {
        DeploymentCircuitBreaker staging = registry.getOrCreate("staging");
        DeploymentCircuitBreaker prod = registry.getOrCreate("prod");
        assertThat(staging).isNotSameAs(prod);
        assertThat(registry.size()).isEqualTo(2);
    }

    @Test
    void getReturnsEmptyForUnknownEnvironment() {
        Optional<DeploymentCircuitBreaker> result = registry.get("unknown");
        assertThat(result).isEmpty();
    }

    @Test
    void registerOverridesExistingBreaker() {
        registry.getOrCreate("dev");
        DeploymentCircuitBreaker custom =
                new DeploymentCircuitBreaker("dev", 5, Duration.ofMinutes(1));
        registry.register(custom);
        assertThat(registry.get("dev")).contains(custom);
        assertThat(registry.size()).isEqualTo(1);
    }

    @Test
    void removeDeletesBreaker() {
        registry.getOrCreate("qa");
        boolean removed = registry.remove("qa");
        assertThat(removed).isTrue();
        assertThat(registry.get("qa")).isEmpty();
    }

    @Test
    void removeMissingBreakerReturnsFalse() {
        assertThat(registry.remove("nonexistent")).isFalse();
    }

    @Test
    void resetAllResetsEveryBreaker() {
        DeploymentCircuitBreaker staging = registry.getOrCreate("staging");
        DeploymentCircuitBreaker prod = registry.getOrCreate("prod");
        staging.recordFailure();
        staging.recordFailure();
        prod.recordFailure();
        prod.recordFailure();
        registry.resetAll();
        assertThat(staging.getFailureCount()).isZero();
        assertThat(prod.getFailureCount()).isZero();
        assertThat(staging.getState()).isEqualTo(CircuitBreakerState.CLOSED);
        assertThat(prod.getState()).isEqualTo(CircuitBreakerState.CLOSED);
    }

    @Test
    void allReturnsUnmodifiableView() {
        registry.getOrCreate("staging");
        assertThatThrownBy(() -> registry.all().put("x",
                new DeploymentCircuitBreaker("x", 1, Duration.ofMinutes(1))))
                .isInstanceOf(UnsupportedOperationException.class);
    }
}
