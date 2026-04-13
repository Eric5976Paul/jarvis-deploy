package com.jarvis.deploy.checkpoint;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class DeploymentCheckpointTest {

    @Test
    void shouldCreateCheckpointWithCorrectFields() {
        Map<String, String> meta = Map.of("artifact", "app-1.0.jar");
        DeploymentCheckpoint cp = new DeploymentCheckpoint("dep-001", "BUILD", CheckpointStatus.PASSED, meta);

        assertEquals("dep-001", cp.getDeploymentId());
        assertEquals("BUILD", cp.getStage());
        assertEquals(CheckpointStatus.PASSED, cp.getStatus());
        assertNotNull(cp.getCapturedAt());
        assertEquals("app-1.0.jar", cp.getMetadata().get("artifact"));
    }

    @Test
    void isPassedReturnsTrueOnlyForPassedStatus() {
        DeploymentCheckpoint passed = new DeploymentCheckpoint("d1", "TEST", CheckpointStatus.PASSED, null);
        DeploymentCheckpoint failed = new DeploymentCheckpoint("d1", "TEST", CheckpointStatus.FAILED, null);
        DeploymentCheckpoint skipped = new DeploymentCheckpoint("d1", "TEST", CheckpointStatus.SKIPPED, null);

        assertTrue(passed.isPassed());
        assertFalse(failed.isPassed());
        assertFalse(skipped.isPassed());
    }

    @Test
    void shouldRejectNullDeploymentId() {
        assertThrows(NullPointerException.class,
                () -> new DeploymentCheckpoint(null, "DEPLOY", CheckpointStatus.PASSED, null));
    }

    @Test
    void metadataShouldBeImmutable() {
        DeploymentCheckpoint cp = new DeploymentCheckpoint("d2", "SMOKE", CheckpointStatus.PASSED,
                Map.of("env", "staging"));
        assertThrows(UnsupportedOperationException.class,
                () -> cp.getMetadata().put("extra", "value"));
    }

    @Test
    void toStringShouldContainKeyFields() {
        DeploymentCheckpoint cp = new DeploymentCheckpoint("d3", "HEALTH", CheckpointStatus.FAILED, null);
        String str = cp.toString();
        assertTrue(str.contains("d3"));
        assertTrue(str.contains("HEALTH"));
        assertTrue(str.contains("FAILED"));
    }
}
