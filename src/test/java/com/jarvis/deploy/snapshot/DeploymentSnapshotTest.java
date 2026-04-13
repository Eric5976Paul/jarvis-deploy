package com.jarvis.deploy.snapshot;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class DeploymentSnapshotTest {

    private static final Map<String, String> SAMPLE_CONFIG = Map.of(
            "server.port", "8080",
            "db.url", "jdbc:postgresql://localhost/mydb"
    );

    @Test
    void shouldGenerateUniqueSnapshotIds() {
        DeploymentSnapshot s1 = new DeploymentSnapshot("prod", "1.0.0", SAMPLE_CONFIG, "alice");
        DeploymentSnapshot s2 = new DeploymentSnapshot("prod", "1.0.0", SAMPLE_CONFIG, "alice");
        assertNotEquals(s1.getSnapshotId(), s2.getSnapshotId());
    }

    @Test
    void shouldStoreFieldsCorrectly() {
        Instant before = Instant.now();
        DeploymentSnapshot snapshot = new DeploymentSnapshot("staging", "2.3.1", SAMPLE_CONFIG, "bob");
        Instant after = Instant.now();

        assertEquals("staging", snapshot.getEnvironment());
        assertEquals("2.3.1", snapshot.getArtifactVersion());
        assertEquals("bob", snapshot.getDeployedBy());
        assertNotNull(snapshot.getSnapshotId());
        assertFalse(snapshot.getCapturedAt().isBefore(before));
        assertFalse(snapshot.getCapturedAt().isAfter(after));
    }

    @Test
    void shouldReturnImmutableConfigProperties() {
        DeploymentSnapshot snapshot = new DeploymentSnapshot("dev", "1.0.0", SAMPLE_CONFIG, "ci");
        Map<String, String> props = snapshot.getConfigProperties();
        assertThrows(UnsupportedOperationException.class, () -> props.put("new.key", "value"));
    }

    @Test
    void shouldDefaultDeployedByToUnknownWhenNull() {
        DeploymentSnapshot snapshot = new DeploymentSnapshot("dev", "1.0.0", SAMPLE_CONFIG, null);
        assertEquals("unknown", snapshot.getDeployedBy());
    }

    @Test
    void shouldThrowOnNullEnvironment() {
        assertThrows(NullPointerException.class,
                () -> new DeploymentSnapshot(null, "1.0.0", SAMPLE_CONFIG, "alice"));
    }

    @Test
    void shouldThrowOnNullArtifactVersion() {
        assertThrows(NullPointerException.class,
                () -> new DeploymentSnapshot("prod", null, SAMPLE_CONFIG, "alice"));
    }

    @Test
    void toStringShouldContainKeyFields() {
        DeploymentSnapshot snapshot = new DeploymentSnapshot("prod", "3.0.0", SAMPLE_CONFIG, "deploy-bot");
        String str = snapshot.toString();
        assertTrue(str.contains("prod"));
        assertTrue(str.contains("3.0.0"));
        assertTrue(str.contains(snapshot.getSnapshotId()));
    }
}
