package com.jarvis.deploy.checkpoint;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CheckpointRegistryTest {

    private CheckpointRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new CheckpointRegistry();
    }

    @Test
    void shouldRecordAndRetrieveCheckpoints() {
        registry.record(new DeploymentCheckpoint("dep-1", "BUILD", CheckpointStatus.PASSED, null));
        registry.record(new DeploymentCheckpoint("dep-1", "TEST", CheckpointStatus.PASSED, null));

        List<DeploymentCheckpoint> checkpoints = registry.getCheckpoints("dep-1");
        assertEquals(2, checkpoints.size());
    }

    @Test
    void allPassedReturnsTrueWhenAllCheckpointsPassed() {
        registry.record(new DeploymentCheckpoint("dep-2", "BUILD", CheckpointStatus.PASSED, null));
        registry.record(new DeploymentCheckpoint("dep-2", "DEPLOY", CheckpointStatus.PASSED, null));

        assertTrue(registry.allPassed("dep-2"));
    }

    @Test
    void allPassedReturnsFalseWhenAnyCheckpointFailed() {
        registry.record(new DeploymentCheckpoint("dep-3", "BUILD", CheckpointStatus.PASSED, null));
        registry.record(new DeploymentCheckpoint("dep-3", "SMOKE", CheckpointStatus.FAILED, null));

        assertFalse(registry.allPassed("dep-3"));
    }

    @Test
    void allPassedReturnsFalseForUnknownDeployment() {
        assertFalse(registry.allPassed("unknown-dep"));
    }

    @Test
    void shouldReturnOnlyFailedCheckpoints() {
        registry.record(new DeploymentCheckpoint("dep-4", "BUILD", CheckpointStatus.PASSED, null));
        registry.record(new DeploymentCheckpoint("dep-4", "TEST", CheckpointStatus.FAILED, null));
        registry.record(new DeploymentCheckpoint("dep-4", "SMOKE", CheckpointStatus.SKIPPED, null));

        List<DeploymentCheckpoint> failed = registry.getFailedCheckpoints("dep-4");
        assertEquals(1, failed.size());
        assertEquals("TEST", failed.get(0).getStage());
    }

    @Test
    void clearShouldRemoveAllCheckpointsForDeployment() {
        registry.record(new DeploymentCheckpoint("dep-5", "BUILD", CheckpointStatus.FAILED, null));
        registry.clear("dep-5");

        assertEquals(0, registry.totalCheckpoints("dep-5"));
        assertFalse(registry.allPassed("dep-5"));
    }

    @Test
    void shouldRejectNullCheckpoint() {
        assertThrows(IllegalArgumentException.class, () -> registry.record(null));
    }

    @Test
    void checkpointsListShouldBeImmutable() {
        registry.record(new DeploymentCheckpoint("dep-6", "BUILD", CheckpointStatus.PASSED, null));
        List<DeploymentCheckpoint> list = registry.getCheckpoints("dep-6");
        assertThrows(UnsupportedOperationException.class,
                () -> list.add(new DeploymentCheckpoint("dep-6", "X", CheckpointStatus.PASSED, null)));
    }
}
