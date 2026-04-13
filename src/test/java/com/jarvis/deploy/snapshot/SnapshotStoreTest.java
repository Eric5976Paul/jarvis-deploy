package com.jarvis.deploy.snapshot;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class SnapshotStoreTest {

    private SnapshotStore store;
    private static final Map<String, String> CONFIG = Map.of("key", "value");

    @BeforeEach
    void setUp() {
        store = new SnapshotStore();
    }

    @Test
    void shouldSaveAndRetrieveLatestSnapshot() {
        DeploymentSnapshot s1 = new DeploymentSnapshot("prod", "1.0.0", CONFIG, "alice");
        DeploymentSnapshot s2 = new DeploymentSnapshot("prod", "1.1.0", CONFIG, "alice");
        store.save(s1);
        store.save(s2);

        Optional<DeploymentSnapshot> latest = store.getLatest("prod");
        assertTrue(latest.isPresent());
        assertEquals("1.1.0", latest.get().getArtifactVersion());
    }

    @Test
    void shouldReturnEmptyWhenNoSnapshotsForEnvironment() {
        Optional<DeploymentSnapshot> result = store.getLatest("nonexistent");
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldFindSnapshotById() {
        DeploymentSnapshot snapshot = new DeploymentSnapshot("staging", "2.0.0", CONFIG, "bob");
        store.save(snapshot);

        Optional<DeploymentSnapshot> found = store.findById(snapshot.getSnapshotId());
        assertTrue(found.isPresent());
        assertEquals(snapshot.getSnapshotId(), found.get().getSnapshotId());
    }

    @Test
    void shouldReturnEmptyForUnknownId() {
        assertTrue(store.findById("does-not-exist").isEmpty());
    }

    @Test
    void shouldListSnapshotsByEnvironmentInDescendingOrder() {
        store.save(new DeploymentSnapshot("dev", "1.0.0", CONFIG, "ci"));
        store.save(new DeploymentSnapshot("dev", "1.1.0", CONFIG, "ci"));
        store.save(new DeploymentSnapshot("dev", "1.2.0", CONFIG, "ci"));

        List<DeploymentSnapshot> list = store.listByEnvironment("dev");
        assertEquals(3, list.size());
        assertEquals("1.2.0", list.get(0).getArtifactVersion());
    }

    @Test
    void shouldNotExceedMaxSnapshotsPerEnvironment() {
        for (int i = 0; i < 12; i++) {
            store.save(new DeploymentSnapshot("prod", "1." + i + ".0", CONFIG, "bot"));
        }
        assertTrue(store.countByEnvironment("prod") <= 10);
    }

    @Test
    void shouldClearSnapshotsForEnvironment() {
        store.save(new DeploymentSnapshot("prod", "1.0.0", CONFIG, "alice"));
        store.clear("prod");
        assertEquals(0, store.countByEnvironment("prod"));
    }

    @Test
    void shouldIsolateSnapshotsBetweenEnvironments() {
        store.save(new DeploymentSnapshot("prod", "1.0.0", CONFIG, "alice"));
        store.save(new DeploymentSnapshot("staging", "2.0.0", CONFIG, "bob"));

        assertEquals(1, store.countByEnvironment("prod"));
        assertEquals(1, store.countByEnvironment("staging"));
    }
}
