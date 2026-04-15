package com.jarvis.deploy.variable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Registry for managing deployment variables with scope-aware resolution.
 * More specific scopes override less specific ones during resolution.
 */
public class VariableRegistry {

    // key: "scope:environment:key" -> variable
    private final Map<String, DeploymentVariable> store = new ConcurrentHashMap<>();

    public void register(DeploymentVariable variable) {
        Objects.requireNonNull(variable, "variable must not be null");
        String storeKey = buildStoreKey(variable.getScope(), variable.getEnvironment(), variable.getKey());
        store.put(storeKey, variable);
    }

    public void set(String key, String value, VariableScope scope, String environment) {
        String storeKey = buildStoreKey(scope, environment, key);
        DeploymentVariable existing = store.get(storeKey);
        if (existing != null) {
            existing.setValue(value);
        } else {
            store.put(storeKey, new DeploymentVariable(key, value, scope, environment));
        }
    }

    /**
     * Resolves a variable by key for a given environment.
     * DEPLOYMENT > ENVIRONMENT > GLOBAL precedence.
     */
    public Optional<DeploymentVariable> resolve(String key, String environment) {
        // Check DEPLOYMENT scope first
        String deployKey = buildStoreKey(VariableScope.DEPLOYMENT, environment, key);
        if (store.containsKey(deployKey)) return Optional.of(store.get(deployKey));

        // Check ENVIRONMENT scope
        String envKey = buildStoreKey(VariableScope.ENVIRONMENT, environment, key);
        if (store.containsKey(envKey)) return Optional.of(store.get(envKey));

        // Fall back to GLOBAL
        String globalKey = buildStoreKey(VariableScope.GLOBAL, null, key);
        return Optional.ofNullable(store.get(globalKey));
    }

    public Optional<String> resolveValue(String key, String environment) {
        return resolve(key, environment).map(DeploymentVariable::getValue);
    }

    public List<DeploymentVariable> listByEnvironment(String environment) {
        return store.values().stream()
                .filter(v -> environment.equals(v.getEnvironment()) ||
                             v.getScope() == VariableScope.GLOBAL)
                .sorted(Comparator.comparing(DeploymentVariable::getKey))
                .collect(Collectors.toList());
    }

    public boolean remove(String key, VariableScope scope, String environment) {
        String storeKey = buildStoreKey(scope, environment, key);
        return store.remove(storeKey) != null;
    }

    public int size() {
        return store.size();
    }

    public void clear() {
        store.clear();
    }

    private String buildStoreKey(VariableScope scope, String environment, String key) {
        return scope.name() + ":" + (environment != null ? environment : "__global__") + ":" + key;
    }
}
