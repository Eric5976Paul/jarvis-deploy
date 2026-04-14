package com.jarvis.deploy.correlation;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * In-memory store for managing and querying deployment correlations.
 */
public class CorrelationStore {

    private final Map<String, DeploymentCorrelation> store = new ConcurrentHashMap<>();

    public void register(DeploymentCorrelation correlation) {
        Objects.requireNonNull(correlation, "correlation must not be null");
        store.put(correlation.getCorrelationId(), correlation);
    }

    public Optional<DeploymentCorrelation> findById(String correlationId) {
        return Optional.ofNullable(store.get(correlationId));
    }

    public List<DeploymentCorrelation> findByEnvironment(String environment) {
        return store.values().stream()
                .filter(c -> c.getEnvironment().equalsIgnoreCase(environment))
                .sorted(Comparator.comparing(DeploymentCorrelation::getCreatedAt))
                .collect(Collectors.toList());
    }

    public List<DeploymentCorrelation> findByService(String serviceName) {
        return store.values().stream()
                .filter(c -> c.getServiceName().equalsIgnoreCase(serviceName))
                .sorted(Comparator.comparing(DeploymentCorrelation::getCreatedAt))
                .collect(Collectors.toList());
    }

    public List<DeploymentCorrelation> findChildren(String parentCorrelationId) {
        return store.values().stream()
                .filter(c -> parentCorrelationId.equals(c.getParentCorrelationId()))
                .collect(Collectors.toList());
    }

    public boolean remove(String correlationId) {
        return store.remove(correlationId) != null;
    }

    public int size() {
        return store.size();
    }

    public void clear() {
        store.clear();
    }
}
