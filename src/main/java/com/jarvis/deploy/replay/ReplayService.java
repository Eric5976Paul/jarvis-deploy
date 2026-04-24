package com.jarvis.deploy.replay;

import com.jarvis.deploy.deployment.DeploymentHistory;
import com.jarvis.deploy.deployment.DeploymentRecord;
import com.jarvis.deploy.deployment.DeploymentService;

import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * Orchestrates the replay of a previous deployment by looking up its record
 * from history and re-executing it against a (potentially different) environment.
 */
public class ReplayService {

    private static final Logger log = Logger.getLogger(ReplayService.class.getName());

    private final DeploymentHistory deploymentHistory;
    private final DeploymentService deploymentService;

    public ReplayService(DeploymentHistory deploymentHistory, DeploymentService deploymentService) {
        this.deploymentHistory = deploymentHistory;
        this.deploymentService = deploymentService;
    }

    public ReplayResult replay(DeploymentReplay replay) {
        log.info("Starting replay: " + replay);

        Optional<DeploymentRecord> sourceOpt = deploymentHistory.findById(replay.getSourceDeploymentId());
        if (sourceOpt.isEmpty()) {
            return ReplayResult.failure(replay.getReplayId(),
                    "Source deployment not found: " + replay.getSourceDeploymentId());
        }

        DeploymentRecord source = sourceOpt.get();

        if (replay.isDryRun()) {
            log.info("Dry-run replay validated for source: " + source.getDeploymentId());
            return ReplayResult.dryRun(replay.getReplayId());
        }

        try {
            String newDeploymentId = UUID.randomUUID().toString();
            deploymentService.deploy(
                    replay.getTargetEnvironment(),
                    source.getArtifactPath(),
                    source.getVersion(),
                    replay.getRequestedBy()
            );
            log.info("Replay succeeded, new deploymentId=" + newDeploymentId);
            return ReplayResult.success(replay.getReplayId(), newDeploymentId);
        } catch (Exception ex) {
            log.severe("Replay failed: " + ex.getMessage());
            return ReplayResult.failure(replay.getReplayId(), ex.getMessage());
        }
    }
}
