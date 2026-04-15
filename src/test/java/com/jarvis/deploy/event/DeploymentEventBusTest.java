package com.jarvis.deploy.event;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DeploymentEventBusTest {

    private DeploymentEventBus eventBus;

    @BeforeEach
    void setUp() {
        eventBus = new DeploymentEventBus();
    }

    @Test
    void publishDeliversEventToSubscriber() {
        List<DeploymentEvent> received = new ArrayList<>();
        eventBus.subscribe(DeploymentEventType.DEPLOYMENT_STARTED, received::add);

        DeploymentEvent event = new DeploymentEvent(DeploymentEventType.DEPLOYMENT_STARTED, "svc-a", "prod");
        eventBus.publish(event);

        assertEquals(1, received.size());
        assertSame(event, received.get(0));
    }

    @Test
    void publishDeliversToMultipleSubscribers() {
        List<DeploymentEvent> first = new ArrayList<>();
        List<DeploymentEvent> second = new ArrayList<>();

        eventBus.subscribe(DeploymentEventType.DEPLOYMENT_SUCCEEDED, first::add);
        eventBus.subscribe(DeploymentEventType.DEPLOYMENT_SUCCEEDED, second::add);

        DeploymentEvent event = new DeploymentEvent(DeploymentEventType.DEPLOYMENT_SUCCEEDED, "svc-b", "staging");
        eventBus.publish(event);

        assertEquals(1, first.size());
        assertEquals(1, second.size());
    }

    @Test
    void publishDoesNotDeliverToUnrelatedSubscribers() {
        List<DeploymentEvent> received = new ArrayList<>();
        eventBus.subscribe(DeploymentEventType.DEPLOYMENT_FAILED, received::add);

        eventBus.publish(new DeploymentEvent(DeploymentEventType.DEPLOYMENT_STARTED, "svc-c", "dev"));

        assertTrue(received.isEmpty());
    }

    @Test
    void unsubscribeAllRemovesListeners() {
        List<DeploymentEvent> received = new ArrayList<>();
        eventBus.subscribe(DeploymentEventType.ROLLBACK_TRIGGERED, received::add);
        eventBus.unsubscribeAll(DeploymentEventType.ROLLBACK_TRIGGERED);

        eventBus.publish(new DeploymentEvent(DeploymentEventType.ROLLBACK_TRIGGERED, "svc-d", "prod"));

        assertTrue(received.isEmpty());
        assertEquals(0, eventBus.listenerCount(DeploymentEventType.ROLLBACK_TRIGGERED));
    }

    @Test
    void listenerCountReturnsCorrectValue() {
        assertEquals(0, eventBus.listenerCount(DeploymentEventType.DEPLOYMENT_STARTED));
        eventBus.subscribe(DeploymentEventType.DEPLOYMENT_STARTED, e -> {});
        eventBus.subscribe(DeploymentEventType.DEPLOYMENT_STARTED, e -> {});
        assertEquals(2, eventBus.listenerCount(DeploymentEventType.DEPLOYMENT_STARTED));
    }

    @Test
    void activeEventTypesReflectsSubscriptions() {
        assertTrue(eventBus.activeEventTypes().isEmpty());
        eventBus.subscribe(DeploymentEventType.DEPLOYMENT_SUCCEEDED, e -> {});
        assertTrue(eventBus.activeEventTypes().contains(DeploymentEventType.DEPLOYMENT_SUCCEEDED));
    }

    @Test
    void publishNullEventThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> eventBus.publish(null));
    }

    @Test
    void subscribeNullTypeThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> eventBus.subscribe(null, e -> {}));
    }

    @Test
    void subscribeNullListenerThrowsException() {
        assertThrows(IllegalArgumentException.class,
                () -> eventBus.subscribe(DeploymentEventType.DEPLOYMENT_STARTED, null));
    }
}
