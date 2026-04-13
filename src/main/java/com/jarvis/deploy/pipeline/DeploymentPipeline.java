package com.jarvis.deploy.pipeline;

import com.jarvis.deploy.artifact.ArtifactResolutionResult;
import com.jarvis.deploy.artifact.ArtifactResolver;
import com.jarvis.deploy.deployment.DeploymentRecord;
import com.jarvis.deploy.deployment.DeploymentService;
import com.jarvis.deploy.env.EnvironmentValidator;
import com.jarvis.deploy.env.ValidationResult;
import com.jarvis.deploy.health.HealthCheckResult;
import com.jarvis.deploy.health.HealthChecker;
import com.jarvis.deploy.lock.DeploymentLockManager;
import com.jarvis.deploy.notification.DeploymentNotifier;
import com.jarvis.deploy.audit.AuditLogger;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Orchestrates the full deployment pipeline: validate -> lock -> resolve -> deploy -> health check -> notify.
 */
public class DeploymentPipeline {

    private final EnvironmentValidator environmentValidator;
    private final ArtifactResolver artifactResolver;
    private final DeploymentService deploymentService;
    private final HealthChecker healthChecker;
    private final DeploymentLockManager lockManager;
    private final DeploymentNotifier notifier;
    private final AuditLogger auditLogger;

    public DeploymentPipeline(EnvironmentValidator environmentValidator,
                               ArtifactResolver artifactResolver,
                               DeploymentService deploymentService,
                               HealthChecker healthChecker,
                               DeploymentLockManager lockManager,
                               DeploymentNotifier notifier,
                               AuditLogger auditLogger) {
        this.environmentValidator = environmentValidator;
        this.artifactResolver = artifactResolver;
        this.deploymentService = deploymentService;
        this.healthChecker = healthChecker;
        this.lockManager = lockManager;
        this.notifier = notifier;
        this.auditLogger = auditLogger;
    }

    public PipelineResult execute(PipelineRequest request) {
        List<String> steps = new ArrayList<>();
        String env = request.getEnvironment();
        String version = request.getVersion();

        // Step 1: Validate environment
        ValidationResult validation = environmentValidator.validate(env);
        if (!validation.isValid()) {
            return PipelineResult.failure("Environment validation failed: " + validation.getErrors(), steps);
        }
        steps.add("environment-validated");

        // Step 2: Acquire deployment lock
        boolean locked = lockManager.acquireLock(env);
        if (!locked) {
            return PipelineResult.failure("Could not acquire deployment lock for environment: " + env, steps);
        }
        steps.add("lock-acquired");

        try {
            // Step 3: Resolve artifact
            ArtifactResolutionResult artifact = artifactResolver.resolve(request.getArtifactId(), version);
            if (!artifact.isResolved()) {
                return PipelineResult.failure("Artifact resolution failed: " + artifact.getErrorMessage(), steps);
            }
            steps.add("artifact-resolved");

            // Step 4: Deploy
            DeploymentRecord record = deploymentService.deploy(env, artifact.getArtifactPath(), version);
            steps.add("deployed");
            auditLogger.log("DEPLOY", env, "Deployed version " + version + " by " + request.getInitiator());

            // Step 5: Health check
            HealthCheckResult health = healthChecker.check(env);
            steps.add("health-checked");

            if (!health.isHealthy()) {
                notifier.notifyFailure(env, version, "Health check failed after deployment");
                return PipelineResult.failure("Post-deployment health check failed: " + health.getMessage(), steps);
            }

            // Step 6: Notify success
            notifier.notifySuccess(env, version);
            steps.add("notified");

            return PipelineResult.success(record, steps);
        } finally {
            lockManager.releaseLock(env);
        }
    }
}
