package com.jarvis.deploy.pipeline;

import com.jarvis.deploy.artifact.ArtifactResolutionResult;
import com.jarvis.deploy.artifact.ArtifactResolver;
import com.jarvis.deploy.audit.AuditLogger;
import com.jarvis.deploy.deployment.DeploymentRecord;
import com.jarvis.deploy.deployment.DeploymentService;
import com.jarvis.deploy.env.EnvironmentValidator;
import com.jarvis.deploy.env.ValidationResult;
import com.jarvis.deploy.health.HealthCheckResult;
import com.jarvis.deploy.health.HealthChecker;
import com.jarvis.deploy.lock.DeploymentLockManager;
import com.jarvis.deploy.notification.DeploymentNotifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeploymentPipelineTest {

    @Mock private EnvironmentValidator environmentValidator;
    @Mock private ArtifactResolver artifactResolver;
    @Mock private DeploymentService deploymentService;
    @Mock private HealthChecker healthChecker;
    @Mock private DeploymentLockManager lockManager;
    @Mock private DeploymentNotifier notifier;
    @Mock private AuditLogger auditLogger;

    private DeploymentPipeline pipeline;

    @BeforeEach
    void setUp() {
        pipeline = new DeploymentPipeline(environmentValidator, artifactResolver,
                deploymentService, healthChecker, lockManager, notifier, auditLogger);
    }

    private PipelineRequest sampleRequest() {
        return PipelineRequest.builder()
                .environment("staging")
                .artifactId("com.example:myapp")
                .version("1.2.3")
                .initiator("ci-bot")
                .build();
    }

    @Test
    void execute_successfulPipeline_returnsSuccessWithAllSteps() {
        when(environmentValidator.validate("staging")).thenReturn(ValidationResult.valid());
        when(lockManager.acquireLock("staging")).thenReturn(true);
        when(artifactResolver.resolve(anyString(), anyString()))
                .thenReturn(ArtifactResolutionResult.resolved("/artifacts/myapp-1.2.3.jar"));
        when(deploymentService.deploy(anyString(), anyString(), anyString()))
                .thenReturn(mock(DeploymentRecord.class));
        when(healthChecker.check("staging")).thenReturn(HealthCheckResult.healthy("OK"));

        PipelineResult result = pipeline.execute(sampleRequest());

        assertThat(result.isSuccessful()).isTrue();
        assertThat(result.getCompletedSteps()).containsExactly(
                "environment-validated", "lock-acquired", "artifact-resolved", "deployed", "health-checked", "notified");
        verify(lockManager).releaseLock("staging");
        verify(notifier).notifySuccess("staging", "1.2.3");
    }

    @Test
    void execute_validationFails_returnsFailureEarly() {
        when(environmentValidator.validate("staging")).thenReturn(ValidationResult.invalid("Unknown environment"));

        PipelineResult result = pipeline.execute(sampleRequest());

        assertThat(result.isSuccessful()).isFalse();
        assertThat(result.getMessage()).contains("validation failed");
        assertThat(result.getCompletedSteps()).isEmpty();
        verifyNoInteractions(lockManager, artifactResolver, deploymentService);
    }

    @Test
    void execute_lockNotAcquired_returnsFailure() {
        when(environmentValidator.validate("staging")).thenReturn(ValidationResult.valid());
        when(lockManager.acquireLock("staging")).thenReturn(false);

        PipelineResult result = pipeline.execute(sampleRequest());

        assertThat(result.isSuccessful()).isFalse();
        assertThat(result.getMessage()).contains("lock");
        assertThat(result.hasCompletedStep("environment-validated")).isTrue();
        assertThat(result.hasCompletedStep("lock-acquired")).isFalse();
    }

    @Test
    void execute_healthCheckFails_releasesLockAndNotifiesFailure() {
        when(environmentValidator.validate("staging")).thenReturn(ValidationResult.valid());
        when(lockManager.acquireLock("staging")).thenReturn(true);
        when(artifactResolver.resolve(anyString(), anyString()))
                .thenReturn(ArtifactResolutionResult.resolved("/artifacts/myapp-1.2.3.jar"));
        when(deploymentService.deploy(anyString(), anyString(), anyString()))
                .thenReturn(mock(DeploymentRecord.class));
        when(healthChecker.check("staging")).thenReturn(HealthCheckResult.unhealthy("Service not responding"));

        PipelineResult result = pipeline.execute(sampleRequest());

        assertThat(result.isSuccessful()).isFalse();
        assertThat(result.getMessage()).contains("health check failed");
        verify(lockManager).releaseLock("staging");
        verify(notifier).notifyFailure(eq("staging"), eq("1.2.3"), anyString());
    }
}
