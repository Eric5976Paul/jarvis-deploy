package com.jarvis.deploy.cache;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A simple time-based in-memory cache for deployment artifacts and metadata.
 * Entries expire after a configurable TTL to avoid stale data during deployments.
 */
public class DeploymentCache {

    private final Duration ttl;
    private final Map<String, CacheEntry> store = new ConcurrentHashMap<>();

    public DeploymentCache(Duration ttl) {
        if (ttl == null || ttl.isNegative() || ttl.isZero()) {
            throw new IllegalArgumentException("TTL must be a positive duration");
        }
        this.ttl = ttl;
    }

    public void put(String key, Object value) {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("Cache key must not be null or blank");
        }
        store.put(key, new CacheEntry(value, Instant.now()));
    }

    public Optional<Object> get(String key) {
        CacheEntry entry = store.get(key);
        if (entry == null) {
            return Optional.empty();
        }
        if (isExpired(entry)) {
            store.remove(key);
            return Optional.empty();
        }
        return Optional.of(entry.value());
    }

    public boolean contains(String key) {
        return get(key).isPresent();
    }

    public void evict(String key) {
        store.remove(key);
    }

    public void evictAll() {
        store.clear();
    }

    public int size() {
        store.entrySet().removeIf(e -> isExpired(e.getValue()));
        return store.size();
    }

    public void evictExpired() {
        store.entrySet().removeIf(e -> isExpired(e.getValue()));
    }

    private boolean isExpired(CacheEntry entry) {
        return Instant.now().isAfter(entry.createdAt().plus(ttl));
    }

    private record CacheEntry(Object value, Instant createdAt) {}
}
