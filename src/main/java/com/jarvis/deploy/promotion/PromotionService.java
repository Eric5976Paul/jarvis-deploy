package com.jarvis.deploy.promotion;

import com.jarvis.deploy.deployment.DeploymentHistory;
import com.jarvis.deploy.deployment.DeploymentRecord;
import com.jarvis.deploy.env.EnvironmentValidator;
import com.jarvis.deploy.env.ValidationResult;

import java.util.Optional;
import java.util.logging.Logger;

/**
 * Orchestrates promotion of a verified artifact from a source environment to a target environment.
 */
public class PromotionService {

    private static final Logger log = Logger.getLogger(PromotionService.class.getName());

    private final EnvironmentValidator environmentValidator;
    private final DeploymentHistory deploymentHistory;

    public PromotionService(EnvironmentValidator environmentValidator,
                            DeploymentHistory deploymentHistory) {
        this.environmentValidator = environmentValidator;
        this.deploymentHistory = deploymentHistory;
    }

    /**
     * Promotes an artifact from source to target environment after validation.
     *
     * @param request the promotion request
     * @return the result of the promotion attempt
     */
    public PromotionResult promote(PromotionRequest request) {
        log.info("Starting promotion: " + request);

        ValidationResult sourceValidation = environmentValidator.validate(request.getSourceEnvironment());
        if (!sourceValidation.isValid()) {
            return PromotionResult.failed(request,
                    "Source environment validation failed: " + sourceValidation.getErrors());
        }

        ValidationResult targetValidation = environmentValidator.validate(request.getTargetEnvironment());
        if (!targetValidation.isValid()) {
            return PromotionResult.failed(request,
                    "Target environment validation failed: " + targetValidation.getErrors());
        }

        Optional<DeploymentRecord> latestInSource = deploymentHistory
                .getLatestRecord(request.getApplicationName(), request.getSourceEnvironment());

        if (latestInSource.isEmpty()) {
            return PromotionResult.skipped(request,
                    "No deployment record found in source environment '" + request.getSourceEnvironment() + "'");
        }

        DeploymentRecord sourceRecord = latestInSource.get();
        if (!sourceRecord.getVersion().equals(request.getArtifactVersion())) {
            return PromotionResult.failed(request,
                    String.format("Version mismatch: requested '%s' but latest in source is '%s'",
                            request.getArtifactVersion(), sourceRecord.getVersion()));
        }

        DeploymentRecord promoted = DeploymentRecord.of(
                request.getApplicationName(),
                request.getArtifactVersion(),
                request.getTargetEnvironment(),
                request.getRequestedBy()
        );
        deploymentHistory.record(promoted);

        log.info("Promotion successful: " + request);
        return PromotionResult.success(request,
                String.format("Successfully promoted '%s' v%s to '%s'",
                        request.getApplicationName(), request.getArtifactVersion(),
                        request.getTargetEnvironment()));
    }
}
