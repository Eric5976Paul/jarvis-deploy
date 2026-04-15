package com.jarvis.deploy.event;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * A simple in-process event bus for broadcasting deployment lifecycle events
 * to registered listeners, keyed by event type.
 */
public class DeploymentEventBus {

    private final Map<DeploymentEventType, List<Consumer<DeploymentEvent>>> listeners =
            new ConcurrentHashMap<>();

    /**
     * Register a listener for a specific event type.
     *
     * @param type     the event type to subscribe to
     * @param listener the callback to invoke when an event of that type is published
     */
    public void subscribe(DeploymentEventType type, Consumer<DeploymentEvent> listener) {
        if (type == null || listener == null) {
            throw new IllegalArgumentException("Event type and listener must not be null");
        }
        listeners.computeIfAbsent(type, k -> new CopyOnWriteArrayList<>()).add(listener);
    }

    /**
     * Remove all listeners for the given event type.
     *
     * @param type the event type whose listeners should be cleared
     */
    public void unsubscribeAll(DeploymentEventType type) {
        if (type != null) {
            listeners.remove(type);
        }
    }

    /**
     * Publish an event to all registered listeners for its type.
     * Listeners are invoked synchronously in subscription order.
     *
     * @param event the event to publish
     */
    public void publish(DeploymentEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("Event must not be null");
        }
        List<Consumer<DeploymentEvent>> registered =
                listeners.getOrDefault(event.getType(), Collections.emptyList());
        for (Consumer<DeploymentEvent> listener : registered) {
            listener.accept(event);
        }
    }

    /**
     * Returns the number of listeners registered for a given event type.
     *
     * @param type the event type
     * @return listener count
     */
    public int listenerCount(DeploymentEventType type) {
        return listeners.getOrDefault(type, Collections.emptyList()).size();
    }

    /**
     * Returns all event types that currently have at least one listener.
     *
     * @return unmodifiable list of active event types
     */
    public List<DeploymentEventType> activeEventTypes() {
        return Collections.unmodifiableList(new ArrayList<>(listeners.keySet()));
    }
}
