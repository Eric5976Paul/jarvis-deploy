package com.jarvis.deploy.pause;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

class DeploymentPauseServiceTest {

    private DeploymentPauseService service;

    @BeforeEach
    void setUp() {
        service = new DeploymentPauseService();
    }

    @Test
    void pause_shouldMarkDeploymentAsPaused() {
        service.pause("deploy-1", "Waiting for approval", "alice", null);
        assertThat(service.isPaused("deploy-1")).isTrue();
    }

    @Test
    void pause_shouldReturnRecordWithCorrectFields() {
        PauseRecord record = service.pause("deploy-2", "Maintenance window", "bob", null);
        assertThat(record.deploymentId()).isEqualTo("deploy-2");
        assertThat(record.reason()).isEqualTo("Maintenance window");
        assertThat(record.pausedBy()).isEqualTo("bob");
        assertThat(record.pausedAt()).isNotNull();
        assertThat(record.expiresAt()).isNull();
    }

    @Test
    void pause_shouldThrowOnBlankDeploymentId() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> service.pause(" ", "reason", "actor", null));
    }

    @Test
    void resume_shouldUnpauseDeployment() {
        service.pause("deploy-3", "Manual hold", "carol", null);
        boolean resumed = service.resume("deploy-3");
        assertThat(resumed).isTrue();
        assertThat(service.isPaused("deploy-3")).isFalse();
    }

    @Test
    void resume_shouldReturnFalseWhenNotPaused() {
        boolean resumed = service.resume("nonexistent");
        assertThat(resumed).isFalse();
    }

    @Test
    void isPaused_shouldReturnFalseForExpiredPause() {
        Instant past = Instant.now().minusSeconds(60);
        service.pause("deploy-4", "Expired hold", "dave", past);
        assertThat(service.isPaused("deploy-4")).isFalse();
    }

    @Test
    void getPauseRecord_shouldReturnRecordForActivePause() {
        service.pause("deploy-5", "Review", "eve", null);
        Optional<PauseRecord> record = service.getPauseRecord("deploy-5");
        assertThat(record).isPresent();
        assertThat(record.get().deploymentId()).isEqualTo("deploy-5");
    }

    @Test
    void getPauseRecord_shouldReturnEmptyWhenNotPaused() {
        Optional<PauseRecord> record = service.getPauseRecord("unknown");
        assertThat(record).isEmpty();
    }

    @Test
    void pausedCount_shouldReflectCurrentPausedDeployments() {
        assertThat(service.pausedCount()).isZero();
        service.pause("d1", "r1", "u1", null);
        service.pause("d2", "r2", "u2", null);
        assertThat(service.pausedCount()).isEqualTo(2);
        service.resume("d1");
        assertThat(service.pausedCount()).isEqualTo(1);
    }
}
