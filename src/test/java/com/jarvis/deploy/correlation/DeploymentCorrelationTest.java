package com.jarvis.deploy.correlation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DeploymentCorrelationTest {

    @Test
    void shouldCreateCorrelationWithGeneratedId() {
        DeploymentCorrelation correlation = new DeploymentCorrelation("prod", "order-service", "ci-bot");
        assertNotNull(correlation.getCorrelationId());
        assertFalse(correlation.getCorrelationId().isBlank());
        assertNull(correlation.getParentCorrelationId());
        assertFalse(correlation.hasParent());
    }

    @Test
    void shouldStoreFieldsCorrectly() {
        DeploymentCorrelation correlation = new DeploymentCorrelation("staging", "payment-service", "alice");
        assertEquals("staging", correlation.getEnvironment());
        assertEquals("payment-service", correlation.getServiceName());
        assertEquals("alice", correlation.getInitiatedBy());
        assertNotNull(correlation.getCreatedAt());
    }

    @Test
    void shouldDeriveChildWithParentReference() {
        DeploymentCorrelation parent = new DeploymentCorrelation("dev", "auth-service", "bob");
        DeploymentCorrelation child = parent.deriveChild();

        assertNotEquals(parent.getCorrelationId(), child.getCorrelationId());
        assertEquals(parent.getCorrelationId(), child.getParentCorrelationId());
        assertTrue(child.hasParent());
        assertEquals(parent.getEnvironment(), child.getEnvironment());
        assertEquals(parent.getServiceName(), child.getServiceName());
    }

    @Test
    void shouldRejectNullEnvironment() {
        assertThrows(NullPointerException.class,
                () -> new DeploymentCorrelation(null, "svc", "user"));
    }

    @Test
    void shouldRejectNullServiceName() {
        assertThrows(NullPointerException.class,
                () -> new DeploymentCorrelation("prod", null, "user"));
    }

    @Test
    void shouldRejectNullInitiatedBy() {
        assertThrows(NullPointerException.class,
                () -> new DeploymentCorrelation("prod", "svc", null));
    }

    @Test
    void toStringShouldContainKeyFields() {
        DeploymentCorrelation correlation = new DeploymentCorrelation("prod", "order-service", "ci-bot");
        String str = correlation.toString();
        assertTrue(str.contains("prod"));
        assertTrue(str.contains("order-service"));
    }
}
