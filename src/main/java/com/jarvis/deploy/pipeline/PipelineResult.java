package com.jarvis.deploy.pipeline;

import com.jarvis.deploy.deployment.DeploymentRecord;

import java.util.Collections;
import java.util.List;

/**
 * Represents the outcome of a full deployment pipeline execution.
 */
public class PipelineResult {

    private final boolean successful;
    private final String message;
    private final DeploymentRecord deploymentRecord;
    private final List<String> completedSteps;

    private PipelineResult(boolean successful, String message, DeploymentRecord deploymentRecord, List<String> completedSteps) {
        this.successful = successful;
        this.message = message;
        this.deploymentRecord = deploymentRecord;
        this.completedSteps = Collections.unmodifiableList(completedSteps);
    }

    public static PipelineResult success(DeploymentRecord record, List<String> steps) {
        return new PipelineResult(true, "Pipeline completed successfully", record, steps);
    }

    public static PipelineResult failure(String message, List<String> steps) {
        return new PipelineResult(false, message, null, steps);
    }

    public boolean isSuccessful() { return successful; }
    public String getMessage() { return message; }
    public DeploymentRecord getDeploymentRecord() { return deploymentRecord; }
    public List<String> getCompletedSteps() { return completedSteps; }

    public int getStepCount() { return completedSteps.size(); }

    public boolean hasCompletedStep(String step) {
        return completedSteps.contains(step);
    }

    @Override
    public String toString() {
        return "PipelineResult{success=" + successful + ", steps=" + completedSteps.size() + ", message='" + message + "'}";
    }
}
