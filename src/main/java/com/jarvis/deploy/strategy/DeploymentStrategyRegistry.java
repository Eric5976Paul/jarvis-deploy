package com.jarvis.deploy.strategy;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Registry for managing named deployment strategies.
 * Supports registration, lookup, and listing of available strategies.
 */
public class DeploymentStrategyRegistry {

    private final Map<String, DeploymentStrategy> strategies = new HashMap<>();

    public void register(String name, DeploymentStrategy strategy) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Strategy name must not be blank");
        }
        if (strategy == null) {
            throw new IllegalArgumentException("Strategy must not be null");
        }
        strategies.put(name.toLowerCase(), strategy);
    }

    /**
     * Registers a strategy only if no strategy is already registered under the given name.
     *
     * @param name     the strategy name
     * @param strategy the strategy to register
     * @return {@code true} if the strategy was registered, {@code false} if a strategy
     *         with that name already existed
     */
    public boolean registerIfAbsent(String name, DeploymentStrategy strategy) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Strategy name must not be blank");
        }
        if (strategy == null) {
            throw new IllegalArgumentException("Strategy must not be null");
        }
        return strategies.putIfAbsent(name.toLowerCase(), strategy) == null;
    }

    public Optional<DeploymentStrategy> find(String name) {
        if (name == null) return Optional.empty();
        return Optional.ofNullable(strategies.get(name.toLowerCase()));
    }

    public DeploymentStrategy getOrDefault(String name, DeploymentStrategy defaultStrategy) {
        return find(name).orElse(defaultStrategy);
    }

    public boolean isRegistered(String name) {
        return name != null && strategies.containsKey(name.toLowerCase());
    }

    public Set<String> listNames() {
        return java.util.Collections.unmodifiableSet(strategies.keySet());
    }

    public int size() {
        return strategies.size();
    }

    public void unregister(String name) {
        if (name != null) {
            strategies.remove(name.toLowerCase());
        }
    }
}
