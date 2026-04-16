package com.jarvis.deploy.throttle;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ThrottleEnforcerTest {

    private ThrottleEnforcer enforcer;

    @BeforeEach
    void setUp() {
        enforcer = new ThrottleEnforcer();
    }

    private DeploymentThrottle throttle(int max, int windowSeconds) {
        DeploymentThrottle t = new DeploymentThrottle();
        t.setMaxDeployments(max);
        t.setWindowSeconds(windowSeconds);
        t.setAction(ThrottleAction.BLOCK);
        return t;
    }

    @Test
    void allowsDeploymentUnderLimit() {
        DeploymentThrottle t = throttle(3, 60);
        ThrottleCheckResult result = enforcer.check("staging", t);
        assertTrue(result.isAllowed());
    }

    @Test
    void blocksDeploymentAtLimit() {
        DeploymentThrottle t = throttle(2, 60);
        enforcer.check("prod", t);
        enforcer.check("prod", t);
        ThrottleCheckResult result = enforcer.check("prod", t);
        assertFalse(result.isAllowed());
        assertEquals(ThrottleAction.BLOCK, result.getAction());
        assertNotNull(result.getReason());
    }

    @Test
    void allowsWhenThrottleIsNull() {
        ThrottleCheckResult result = enforcer.check("dev", null);
        assertTrue(result.isAllowed());
    }

    @Test
    void resetClearsCountForEnvironment() {
        DeploymentThrottle t = throttle(1, 60);
        enforcer.check("staging", t);
        enforcer.reset("staging");
        ThrottleCheckResult result = enforcer.check("staging", t);
        assertTrue(result.isAllowed());
    }

    @Test
    void currentCountReflectsActiveDeployments() {
        DeploymentThrottle t = throttle(5, 60);
        enforcer.check("dev", t);
        enforcer.check("dev", t);
        assertEquals(2, enforcer.currentCount("dev", t));
    }

    @Test
    void separateEnvironmentsAreIndependent() {
        DeploymentThrottle t = throttle(1, 60);
        enforcer.check("env-a", t);
        ThrottleCheckResult result = enforcer.check("env-b", t);
        assertTrue(result.isAllowed());
    }
}
