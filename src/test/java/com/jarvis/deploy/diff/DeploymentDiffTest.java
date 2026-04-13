package com.jarvis.deploy.diff;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DeploymentDiffTest {

    @Test
    void shouldCreateDiffWithNoChanges() {
        DeploymentDiff diff = new DeploymentDiff("1.0.0", "1.1.0", "staging");
        assertFalse(diff.hasChanges());
        assertEquals(0, diff.getChangeCount());
    }

    @Test
    void shouldTrackAddedEntry() {
        DeploymentDiff diff = new DeploymentDiff("1.0.0", "1.1.0", "prod");
        diff.addChange("NEW_FEATURE_FLAG", null, "true");

        assertTrue(diff.hasChanges());
        assertEquals(1, diff.getChangeCount());
        DiffEntry entry = diff.getChanges().get("NEW_FEATURE_FLAG");
        assertNotNull(entry);
        assertEquals(DiffEntry.ChangeType.ADDED, entry.getChangeType());
    }

    @Test
    void shouldTrackRemovedEntry() {
        DeploymentDiff diff = new DeploymentDiff("1.1.0", "1.2.0", "dev");
        diff.addChange("OLD_CONFIG", "someValue", null);

        DiffEntry entry = diff.getChanges().get("OLD_CONFIG");
        assertEquals(DiffEntry.ChangeType.REMOVED, entry.getChangeType());
        assertNull(entry.getNewValue());
    }

    @Test
    void shouldTrackModifiedEntry() {
        DeploymentDiff diff = new DeploymentDiff("1.0.0", "1.1.0", "staging");
        diff.addChange("DB_POOL_SIZE", "10", "20");

        DiffEntry entry = diff.getChanges().get("DB_POOL_SIZE");
        assertEquals(DiffEntry.ChangeType.MODIFIED, entry.getChangeType());
        assertEquals("10", entry.getOldValue());
        assertEquals("20", entry.getNewValue());
    }

    @Test
    void shouldReturnImmutableChangesMap() {
        DeploymentDiff diff = new DeploymentDiff("1.0.0", "1.1.0", "prod");
        diff.addChange("key", "old", "new");
        assertThrows(UnsupportedOperationException.class,
                () -> diff.getChanges().put("another", null));
    }

    @Test
    void shouldRejectNullVersions() {
        assertThrows(NullPointerException.class, () -> new DeploymentDiff(null, "1.1.0", "prod"));
        assertThrows(NullPointerException.class, () -> new DeploymentDiff("1.0.0", null, "prod"));
        assertThrows(NullPointerException.class, () -> new DeploymentDiff("1.0.0", "1.1.0", null));
    }
}
