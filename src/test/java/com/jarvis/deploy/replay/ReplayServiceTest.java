package com.jarvis.deploy.replay;

import com.jarvis.deploy.deployment.DeploymentHistory;
import com.jarvis.deploy.deployment.DeploymentRecord;
import com.jarvis.deploy.deployment.DeploymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReplayServiceTest {

    @Mock private DeploymentHistory deploymentHistory;
    @Mock private DeploymentService deploymentService;

    private ReplayService replayService;

    @BeforeEach
    void setUp() {
        replayService = new ReplayService(deploymentHistory, deploymentService);
    }

    @Test
    void replay_success() {
        DeploymentRecord record = mock(DeploymentRecord.class);
        when(record.getDeploymentId()).thenReturn("dep-001");
        when(record.getArtifactPath()).thenReturn("/artifacts/app-1.0.jar");
        when(record.getVersion()).thenReturn("1.0.0");
        when(deploymentHistory.findById("dep-001")).thenReturn(Optional.of(record));

        DeploymentReplay replay = new DeploymentReplay("rpl-1", "dep-001", "staging", "alice", false);
        ReplayResult result = replayService.replay(replay);

        assertThat(result.isSuccessful()).isTrue();
        assertThat(result.getStatus()).isEqualTo(ReplayResult.Status.SUCCESS);
        assertThat(result.getNewDeploymentId()).isPresent();
        verify(deploymentService).deploy(eq("staging"), eq("/artifacts/app-1.0.jar"), eq("1.0.0"), eq("alice"));
    }

    @Test
    void replay_sourceNotFound_returnsFailure() {
        when(deploymentHistory.findById("dep-999")).thenReturn(Optional.empty());

        DeploymentReplay replay = new DeploymentReplay("rpl-2", "dep-999", "prod", "bob", false);
        ReplayResult result = replayService.replay(replay);

        assertThat(result.isSuccessful()).isFalse();
        assertThat(result.getStatus()).isEqualTo(ReplayResult.Status.FAILED);
        assertThat(result.getMessage()).contains("dep-999");
        verifyNoInteractions(deploymentService);
    }

    @Test
    void replay_dryRun_doesNotDeploy() {
        DeploymentRecord record = mock(DeploymentRecord.class);
        when(record.getDeploymentId()).thenReturn("dep-002");
        when(deploymentHistory.findById("dep-002")).thenReturn(Optional.of(record));

        DeploymentReplay replay = new DeploymentReplay("rpl-3", "dep-002", "staging", "carol", true);
        ReplayResult result = replayService.replay(replay);

        assertThat(result.getStatus()).isEqualTo(ReplayResult.Status.SKIPPED_DRY_RUN);
        assertThat(result.isSuccessful()).isTrue();
        assertThat(result.getNewDeploymentId()).isEmpty();
        verifyNoInteractions(deploymentService);
    }

    @Test
    void replay_deployThrows_returnsFailure() {
        DeploymentRecord record = mock(DeploymentRecord.class);
        when(record.getArtifactPath()).thenReturn("/artifacts/app.jar");
        when(record.getVersion()).thenReturn("2.0.0");
        when(deploymentHistory.findById("dep-003")).thenReturn(Optional.of(record));
        doThrow(new RuntimeException("deploy error")).when(deploymentService)
                .deploy(anyString(), anyString(), anyString(), anyString());

        DeploymentReplay replay = new DeploymentReplay("rpl-4", "dep-003", "prod", "dave", false);
        ReplayResult result = replayService.replay(replay);

        assertThat(result.isSuccessful()).isFalse();
        assertThat(result.getMessage()).contains("deploy error");
    }
}
