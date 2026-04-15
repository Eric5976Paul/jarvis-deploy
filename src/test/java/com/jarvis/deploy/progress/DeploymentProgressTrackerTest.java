package com.jarvis.deploy.progress;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DeploymentProgressTrackerTest {

    private DeploymentProgressTracker tracker;

    @BeforeEach
    void setUp() {
        tracker = new DeploymentProgressTracker("deploy-42");
    }

    @Test
    void constructorRejectsBlankId() {
        assertThrows(IllegalArgumentException.class, () -> new DeploymentProgressTracker(""));
        assertThrows(IllegalArgumentException.class, () -> new DeploymentProgressTracker(null));
    }

    @Test
    void initialStateIsZeroPercent() {
        assertEquals(0, tracker.getOverallPercent());
        assertFalse(tracker.isCompleted());
        assertFalse(tracker.isFailed());
        assertNull(tracker.getCurrentStage());
    }

    @Test
    void registerAndBeginStage() {
        tracker.registerStage("validate", 0);
        tracker.beginStage("validate");

        assertEquals("validate", tracker.getCurrentStage());
        assertTrue(tracker.getStage("validate").isPresent());
        assertEquals(DeploymentProgressStage.Status.IN_PROGRESS,
                tracker.getStage("validate").get().getStatus());
    }

    @Test
    void updateStageProgressAffectsOverall() {
        tracker.registerStage("build", 0);
        tracker.registerStage("deploy", 1);
        tracker.updateStageProgress("build", 100);
        tracker.updateStageProgress("deploy", 50);

        assertEquals(75, tracker.getOverallPercent());
    }

    @Test
    void completeStageSetsPctTo100() {
        tracker.registerStage("smoke-test", 0);
        tracker.completeStage("smoke-test");

        DeploymentProgressStage stage = tracker.getStage("smoke-test").orElseThrow();
        assertEquals(100, stage.getPercent());
        assertEquals(DeploymentProgressStage.Status.COMPLETED, stage.getStatus());
    }

    @Test
    void failStageMarksBothStageAndTrackerAsFailed() {
        tracker.registerStage("deploy", 0);
        tracker.failStage("deploy", "connection refused");

        assertTrue(tracker.isFailed());
        DeploymentProgressStage stage = tracker.getStage("deploy").orElseThrow();
        assertEquals(DeploymentProgressStage.Status.FAILED, stage.getStatus());
        assertEquals("connection refused", stage.getFailureReason());
    }

    @Test
    void markCompletedSetsHundredPercent() {
        tracker.markCompleted();
        assertEquals(100, tracker.getOverallPercent());
        assertTrue(tracker.isCompleted());
    }

    @Test
    void invalidPercentThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> tracker.updateStageProgress("x", 101));
        assertThrows(IllegalArgumentException.class,
                () -> tracker.updateStageProgress("x", -1));
    }

    @Test
    void getAllStagesReturnsUnmodifiableView() {
        tracker.registerStage("a", 0);
        tracker.registerStage("b", 1);
        assertEquals(2, tracker.getAllStages().size());
        assertThrows(UnsupportedOperationException.class,
                () -> tracker.getAllStages().put("c", null));
    }
}
