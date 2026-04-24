package com.jarvis.deploy.replay;

import java.time.Instant;
import java.util.Optional;

/**
 * Encapsulates the outcome of a deployment replay operation.
 */
public class ReplayResult {

    public enum Status { SUCCESS, SKIPPED_DRY_RUN, FAILED }

    private final String replayId;
    private final Status status;
    private final String message;
    private final String newDeploymentId;
    private final Instant completedAt;

    private ReplayResult(String replayId, Status status, String message, String newDeploymentId) {
        this.replayId = replayId;
        this.status = status;
        this.message = message;
        this.newDeploymentId = newDeploymentId;
        this.completedAt = Instant.now();
    }

    public static ReplayResult success(String replayId, String newDeploymentId) {
        return new ReplayResult(replayId, Status.SUCCESS, "Replay completed successfully.", newDeploymentId);
    }

    public static ReplayResult dryRun(String replayId) {
        return new ReplayResult(replayId, Status.SKIPPED_DRY_RUN, "Dry-run: replay validated but not executed.", null);
    }

    public static ReplayResult failure(String replayId, String reason) {
        return new ReplayResult(replayId, Status.FAILED, reason, null);
    }

    public boolean isSuccessful() {
        return status == Status.SUCCESS || status == Status.SKIPPED_DRY_RUN;
    }

    public String getReplayId() { return replayId; }
    public Status getStatus() { return status; }
    public String getMessage() { return message; }
    public Optional<String> getNewDeploymentId() { return Optional.ofNullable(newDeploymentId); }
    public Instant getCompletedAt() { return completedAt; }

    @Override
    public String toString() {
        return String.format("ReplayResult{replayId='%s', status=%s, message='%s'}", replayId, status, message);
    }
}
