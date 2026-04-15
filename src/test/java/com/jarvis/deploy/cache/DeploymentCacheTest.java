package com.jarvis.deploy.cache;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class DeploymentCacheTest {

    private DeploymentCache cache;

    @BeforeEach
    void setUp() {
        cache = new DeploymentCache(Duration.ofMinutes(5));
    }

    @Test
    void shouldStoreAndRetrieveValue() {
        cache.put("env:prod", "v1.2.3");
        Optional<Object> result = cache.get("env:prod");
        assertTrue(result.isPresent());
        assertEquals("v1.2.3", result.get());
    }

    @Test
    void shouldReturnEmptyForMissingKey() {
        Optional<Object> result = cache.get("nonexistent");
        assertFalse(result.isPresent());
    }

    @Test
    void shouldEvictEntryExplicitly() {
        cache.put("env:staging", "v1.1.0");
        cache.evict("env:staging");
        assertFalse(cache.contains("env:staging"));
    }

    @Test
    void shouldEvictAllEntries() {
        cache.put("key1", "val1");
        cache.put("key2", "val2");
        cache.evictAll();
        assertEquals(0, cache.size());
    }

    @Test
    void shouldExpireEntryAfterTtl() throws InterruptedException {
        DeploymentCache shortLivedCache = new DeploymentCache(Duration.ofMillis(50));
        shortLivedCache.put("temp", "data");
        Thread.sleep(100);
        assertFalse(shortLivedCache.contains("temp"));
    }

    @Test
    void shouldReportCorrectSizeExcludingExpired() throws InterruptedException {
        DeploymentCache shortLivedCache = new DeploymentCache(Duration.ofMillis(50));
        shortLivedCache.put("a", 1);
        shortLivedCache.put("b", 2);
        Thread.sleep(100);
        shortLivedCache.put("c", 3);
        assertEquals(1, shortLivedCache.size());
    }

    @Test
    void shouldThrowOnNullOrBlankKey() {
        assertThrows(IllegalArgumentException.class, () -> cache.put(null, "value"));
        assertThrows(IllegalArgumentException.class, () -> cache.put("  ", "value"));
    }

    @Test
    void shouldThrowOnInvalidTtl() {
        assertThrows(IllegalArgumentException.class, () -> new DeploymentCache(Duration.ZERO));
        assertThrows(IllegalArgumentException.class, () -> new DeploymentCache(Duration.ofSeconds(-1)));
        assertThrows(IllegalArgumentException.class, () -> new DeploymentCache(null));
    }

    @Test
    void shouldOverwriteExistingKey() {
        cache.put("env:dev", "v1.0.0");
        cache.put("env:dev", "v2.0.0");
        Optional<Object> result = cache.get("env:dev");
        assertTrue(result.isPresent());
        assertEquals("v2.0.0", result.get());
    }
}
