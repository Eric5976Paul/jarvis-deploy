package com.jarvis.deploy.tag;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Registry for managing {@link DeploymentTag} instances.
 * Supports adding, removing, and querying tags by name or environment.
 */
public class DeploymentTagRegistry {

    // key: "<environment>:<name>"
    private final Map<String, DeploymentTag> store = new ConcurrentHashMap<>();

    public void register(DeploymentTag tag) {
        if (tag == null) {
            throw new IllegalArgumentException("Tag must not be null");
        }
        store.put(key(tag.getEnvironment(), tag.getName()), tag);
    }

    public boolean remove(String environment, String name) {
        return store.remove(key(environment, name)) != null;
    }

    public Optional<DeploymentTag> find(String environment, String name) {
        return Optional.ofNullable(store.get(key(environment, name)));
    }

    public List<DeploymentTag> findByEnvironment(String environment) {
        if (environment == null) return Collections.emptyList();
        return store.values().stream()
                .filter(t -> environment.equals(t.getEnvironment()))
                .collect(Collectors.toList());
    }

    public List<DeploymentTag> findByLabel(String labelKey, String labelValue) {
        return store.values().stream()
                .filter(t -> labelValue.equals(t.getLabel(labelKey)))
                .collect(Collectors.toList());
    }

    public List<DeploymentTag> all() {
        return new ArrayList<>(store.values());
    }

    public int size() {
        return store.size();
    }

    public void clear() {
        store.clear();
    }

    private String key(String environment, String name) {
        return environment + ":" + name;
    }
}
