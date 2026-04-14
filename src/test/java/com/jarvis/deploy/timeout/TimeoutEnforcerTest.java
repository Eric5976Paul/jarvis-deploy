package com.jarvis.deploy.timeout;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class TimeoutEnforcerTest {

    private TimeoutEnforcer enforcer;

    @BeforeEach
    void setUp() {
        enforcer = new TimeoutEnforcer();
    }

    @Test
    void register_shouldTrackDeployment() {
        enforcer.register("deploy-1", Duration.ofMinutes(5));
        assertEquals(1, enforcer.activeCount());
    }

    @Test
    void register_shouldReturnTimeout() {
        DeploymentTimeout timeout = enforcer.register("deploy-2", Duration.ofSeconds(30));
        assertNotNull(timeout);
        assertEquals("deploy-2", timeout.getDeploymentId());
        assertEquals(Duration.ofSeconds(30), timeout.getMaxDuration());
    }

    @Test
    void isExceeded_shouldReturnFalseForFreshTimeout() {
        enforcer.register("deploy-3", Duration.ofMinutes(10));
        assertFalse(enforcer.isExceeded("deploy-3"));
    }

    @Test
    void isExceeded_shouldReturnTrueForExpiredTimeout() {
        enforcer.register("deploy-4", Duration.ofNanos(1));
        // Sleep briefly to ensure the nanosecond duration passes
        try { Thread.sleep(5); } catch (InterruptedException ignored) {}
        assertTrue(enforcer.isExceeded("deploy-4"));
    }

    @Test
    void isExceeded_shouldThrowForUnknownDeployment() {
        assertThrows(IllegalArgumentException.class, () -> enforcer.isExceeded("unknown"));
    }

    @Test
    void getRemaining_shouldBePositiveForActiveTimeout() {
        enforcer.register("deploy-5", Duration.ofMinutes(5));
        Duration remaining = enforcer.getRemaining("deploy-5");
        assertTrue(remaining.toSeconds() > 0);
    }

    @Test
    void getRemaining_shouldBeZeroForExpiredTimeout() {
        enforcer.register("deploy-6", Duration.ofNanos(1));
        try { Thread.sleep(5); } catch (InterruptedException ignored) {}
        assertEquals(Duration.ZERO, enforcer.getRemaining("deploy-6"));
    }

    @Test
    void release_shouldRemoveTimeout() {
        enforcer.register("deploy-7", Duration.ofMinutes(2));
        Optional<DeploymentTimeout> released = enforcer.release("deploy-7");
        assertTrue(released.isPresent());
        assertEquals(0, enforcer.activeCount());
    }

    @Test
    void release_shouldReturnEmptyForUnregisteredDeployment() {
        Optional<DeploymentTimeout> result = enforcer.release("nonexistent");
        assertFalse(result.isPresent());
    }

    @Test
    void find_shouldReturnRegisteredTimeout() {
        enforcer.register("deploy-8", Duration.ofMinutes(1));
        Optional<DeploymentTimeout> found = enforcer.find("deploy-8");
        assertTrue(found.isPresent());
        assertEquals("deploy-8", found.get().getDeploymentId());
    }

    @Test
    void find_shouldReturnEmptyForUnknown() {
        assertFalse(enforcer.find("missing").isPresent());
    }

    @Test
    void register_shouldRejectBlankDeploymentId() {
        assertThrows(IllegalArgumentException.class,
            () -> enforcer.register("", Duration.ofMinutes(1)));
    }

    @Test
    void register_shouldRejectZeroDuration() {
        assertThrows(IllegalArgumentException.class,
            () -> enforcer.register("deploy-9", Duration.ZERO));
    }
}
