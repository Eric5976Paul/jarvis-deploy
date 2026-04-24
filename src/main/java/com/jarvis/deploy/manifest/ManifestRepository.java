package com.jarvis.deploy.manifest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * In-memory repository for storing and querying deployment manifests.
 */
public class ManifestRepository {

    private final Map<String, DeploymentManifest> store = new ConcurrentHashMap<>();

    public void save(DeploymentManifest manifest) {
        if (manifest == null) {
            throw new IllegalArgumentException("manifest must not be null");
        }
        store.put(manifest.getManifestId(), manifest);
    }

    public Optional<DeploymentManifest> findById(String manifestId) {
        if (manifestId == null || manifestId.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(store.get(manifestId));
    }

    public List<DeploymentManifest> findByEnvironment(String environment) {
        if (environment == null || environment.isBlank()) {
            return Collections.emptyList();
        }
        return store.values().stream()
                .filter(m -> environment.equals(m.getEnvironment()))
                .collect(Collectors.toList());
    }

    public List<DeploymentManifest> findByStatus(ManifestStatus status) {
        if (status == null) {
            return Collections.emptyList();
        }
        return store.values().stream()
                .filter(m -> status == m.getStatus())
                .collect(Collectors.toList());
    }

    public List<DeploymentManifest> findAll() {
        return new ArrayList<>(store.values());
    }

    public boolean delete(String manifestId) {
        return store.remove(manifestId) != null;
    }

    public int count() {
        return store.size();
    }

    public void clear() {
        store.clear();
    }
}
