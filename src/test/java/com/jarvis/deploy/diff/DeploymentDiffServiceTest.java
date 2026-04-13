package com.jarvis.deploy.diff;

import com.jarvis.deploy.snapshot.DeploymentSnapshot;
import com.jarvis.deploy.snapshot.SnapshotStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeploymentDiffServiceTest {

    @Mock
    private SnapshotStore snapshotStore;

    private DeploymentDiffService diffService;

    @BeforeEach
    void setUp() {
        diffService = new DeploymentDiffService(snapshotStore);
    }

    @Test
    void shouldReturnEmptyWhenFromSnapshotMissing() {
        when(snapshotStore.findByVersion("prod", "1.0.0")).thenReturn(Optional.empty());
        when(snapshotStore.findByVersion("prod", "1.1.0")).thenReturn(Optional.of(mockSnapshot(Map.of())));

        Optional<DeploymentDiff> result = diffService.diff("prod", "1.0.0", "1.1.0");
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldReturnEmptyWhenToSnapshotMissing() {
        when(snapshotStore.findByVersion("prod", "1.0.0")).thenReturn(Optional.of(mockSnapshot(Map.of())));
        when(snapshotStore.findByVersion("prod", "1.1.0")).thenReturn(Optional.empty());

        Optional<DeploymentDiff> result = diffService.diff("prod", "1.0.0", "1.1.0");
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldDetectModifiedProperties() {
        DeploymentSnapshot from = mockSnapshot(Map.of("PORT", "8080", "LOG_LEVEL", "INFO"));
        DeploymentSnapshot to = mockSnapshot(Map.of("PORT", "9090", "LOG_LEVEL", "INFO"));

        when(snapshotStore.findByVersion("staging", "1.0.0")).thenReturn(Optional.of(from));
        when(snapshotStore.findByVersion("staging", "1.1.0")).thenReturn(Optional.of(to));

        Optional<DeploymentDiff> result = diffService.diff("staging", "1.0.0", "1.1.0");
        assertTrue(result.isPresent());
        DeploymentDiff diff = result.get();
        assertEquals(1, diff.getChangeCount());
        assertEquals(DiffEntry.ChangeType.MODIFIED, diff.getChanges().get("PORT").getChangeType());
    }

    @Test
    void shouldReturnNoDiffForIdenticalSnapshots() {
        Map<String, String> props = Map.of("KEY", "value");
        when(snapshotStore.findByVersion("dev", "1.0.0")).thenReturn(Optional.of(mockSnapshot(props)));
        when(snapshotStore.findByVersion("dev", "1.0.1")).thenReturn(Optional.of(mockSnapshot(props)));

        Optional<DeploymentDiff> result = diffService.diff("dev", "1.0.0", "1.0.1");
        assertTrue(result.isPresent());
        assertFalse(result.get().hasChanges());
    }

    private DeploymentSnapshot mockSnapshot(Map<String, String> properties) {
        DeploymentSnapshot snapshot = mock(DeploymentSnapshot.class);
        when(snapshot.getProperties()).thenReturn(properties);
        return snapshot;
    }
}
