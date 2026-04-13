package com.jarvis.deploy.rollback;

import com.jarvis.deploy.deployment.DeploymentHistory;
import com.jarvis.deploy.deployment.DeploymentRecord;
import com.jarvis.deploy.deployment.DeploymentService;

import java.util.Optional;
import java.util.logging.Logger;

/**
 * Service responsible for rolling back deployments to a previous stable state.
 */
public class RollbackService {

    private static final Logger logger = Logger.getLogger(RollbackService.class.getName());

    private final DeploymentHistory deploymentHistory;
    private final DeploymentService deploymentService;

    public RollbackService(DeploymentHistory deploymentHistory, DeploymentService deploymentService) {
        this.deploymentHistory = deploymentHistory;
        this.deploymentService = deploymentService;
    }

    /**
     * Rolls back the given environment to the most recent successful deployment
     * that is not the current one.
     *
     * @param environment the target environment (e.g. "staging", "production")
     * @return the RollbackResult describing what happened
     */
    public RollbackResult rollback(String environment) {
        if (environment == null || environment.isBlank()) {
            throw new IllegalArgumentException("Environment must not be null or blank");
        }

        Optional<DeploymentRecord> target = deploymentHistory.getPreviousSuccessful(environment);

        if (target.isEmpty()) {
            logger.warning("No previous successful deployment found for environment: " + environment);
            return RollbackResult.failure(environment, "No previous successful deployment available");
        }

        DeploymentRecord record = target.get();
        logger.info("Rolling back environment '" + environment + "' to version: " + record.getVersion());

        boolean success = deploymentService.deploy(environment, record.getVersion(), record.getArtifactPath());

        if (success) {
            logger.info("Rollback successful for environment '" + environment + "' to version " + record.getVersion());
            return RollbackResult.success(environment, record.getVersion());
        } else {
            logger.severe("Rollback failed for environment '" + environment + "'");
            return RollbackResult.failure(environment, "Deployment of version " + record.getVersion() + " failed during rollback");
        }
    }
}
