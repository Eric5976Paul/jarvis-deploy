package com.jarvis.deploy.cleanup;

import com.jarvis.deploy.deployment.DeploymentHistory;
import com.jarvis.deploy.deployment.DeploymentRecord;
import com.jarvis.deploy.snapshot.SnapshotStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class DeploymentCleanupServiceTest {

    private DeploymentHistory deploymentHistory;
    private SnapshotStore snapshotStore;
    private DeploymentCleanupService cleanupService;

    @BeforeEach
    void setUp() {
        deploymentHistory = mock(DeploymentHistory.class);
        snapshotStore = mock(SnapshotStore.class);
        cleanupService = new DeploymentCleanupService(deploymentHistory, snapshotStore, 30);
    }

    @Test
    void shouldRemoveStaleRecordsAndSnapshots() {
        DeploymentRecord stale = mock(DeploymentRecord.class);
        when(stale.getDeploymentId()).thenReturn("dep-001");
        when(stale.getTimestamp()).thenReturn(Instant.now().minus(60, ChronoUnit.DAYS));

        when(deploymentHistory.getRecordsForEnvironment("prod")).thenReturn(List.of(stale));
        when(deploymentHistory.removeRecord("dep-001")).thenReturn(true);
        when(snapshotStore.deleteSnapshot("dep-001")).thenReturn(true);

        CleanupResult result = cleanupService.cleanup("prod");

        assertThat(result.getEnvironment()).isEqualTo("prod");
        assertThat(result.getRemovedRecordIds()).containsExactly("dep-001");
        assertThat(result.getRemovedSnapshotIds()).containsExactly("dep-001");
    }

    @Test
    void shouldNotRemoveRecentRecords() {
        DeploymentRecord recent = mock(DeploymentRecord.class);
        when(recent.getDeploymentId()).thenReturn("dep-002");
        when(recent.getTimestamp()).thenReturn(Instant.now().minus(5, ChronoUnit.DAYS));

        when(deploymentHistory.getRecordsForEnvironment("staging")).thenReturn(List.of(recent));

        CleanupResult result = cleanupService.cleanup("staging");

        assertThat(result.getRemovedRecordIds()).isEmpty();
        verify(deploymentHistory, never()).removeRecord(any());
    }

    @Test
    void shouldRejectNonPositiveRetentionDays() {
        assertThatThrownBy(() -> new DeploymentCleanupService(deploymentHistory, snapshotStore, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("retentionDays must be positive");
    }

    @Test
    void shouldReturnRetentionDays() {
        assertThat(cleanupService.getRetentionDays()).isEqualTo(30);
    }
}
