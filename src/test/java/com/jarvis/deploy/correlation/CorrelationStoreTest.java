package com.jarvis.deploy.correlation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class CorrelationStoreTest {

    private CorrelationStore store;

    @BeforeEach
    void setUp() {
        store = new CorrelationStore();
    }

    @Test
    void shouldRegisterAndFindById() {
        DeploymentCorrelation correlation = new DeploymentCorrelation("prod", "svc-a", "user");
        store.register(correlation);

        Optional<DeploymentCorrelation> found = store.findById(correlation.getCorrelationId());
        assertTrue(found.isPresent());
        assertEquals(correlation.getCorrelationId(), found.get().getCorrelationId());
    }

    @Test
    void shouldReturnEmptyForUnknownId() {
        assertTrue(store.findById("nonexistent").isEmpty());
    }

    @Test
    void shouldFindByEnvironment() {
        store.register(new DeploymentCorrelation("prod", "svc-a", "user"));
        store.register(new DeploymentCorrelation("prod", "svc-b", "user"));
        store.register(new DeploymentCorrelation("staging", "svc-c", "user"));

        List<DeploymentCorrelation> prodResults = store.findByEnvironment("prod");
        assertEquals(2, prodResults.size());
        assertTrue(prodResults.stream().allMatch(c -> c.getEnvironment().equals("prod")));
    }

    @Test
    void shouldFindByService() {
        store.register(new DeploymentCorrelation("prod", "order-service", "user"));
        store.register(new DeploymentCorrelation("staging", "order-service", "user"));
        store.register(new DeploymentCorrelation("prod", "payment-service", "user"));

        List<DeploymentCorrelation> results = store.findByService("order-service");
        assertEquals(2, results.size());
    }

    @Test
    void shouldFindChildren() {
        DeploymentCorrelation parent = new DeploymentCorrelation("prod", "svc", "user");
        DeploymentCorrelation child1 = parent.deriveChild();
        DeploymentCorrelation child2 = parent.deriveChild();
        store.register(parent);
        store.register(child1);
        store.register(child2);

        List<DeploymentCorrelation> children = store.findChildren(parent.getCorrelationId());
        assertEquals(2, children.size());
    }

    @Test
    void shouldRemoveCorrelation() {
        DeploymentCorrelation correlation = new DeploymentCorrelation("prod", "svc", "user");
        store.register(correlation);
        assertEquals(1, store.size());

        boolean removed = store.remove(correlation.getCorrelationId());
        assertTrue(removed);
        assertEquals(0, store.size());
    }

    @Test
    void shouldReturnFalseWhenRemovingNonExistent() {
        assertFalse(store.remove("ghost-id"));
    }

    @Test
    void shouldClearAllEntries() {
        store.register(new DeploymentCorrelation("prod", "svc", "user"));
        store.register(new DeploymentCorrelation("staging", "svc", "user"));
        store.clear();
        assertEquals(0, store.size());
    }
}
