package com.jarvis.deploy.approval;

import java.time.Instant;
import java.util.Objects;

/**
 * Represents a deployment approval request for gated environments.
 */
public class ApprovalRequest {

    private final String requestId;
    private final String environment;
    private final String artifactVersion;
    private final String requestedBy;
    private final Instant requestedAt;
    private ApprovalStatus status;
    private String reviewedBy;
    private String reviewNote;

    public ApprovalRequest(String requestId, String environment, String artifactVersion, String requestedBy) {
        this.requestId = Objects.requireNonNull(requestId, "requestId must not be null");
        this.environment = Objects.requireNonNull(environment, "environment must not be null");
        this.artifactVersion = Objects.requireNonNull(artifactVersion, "artifactVersion must not be null");
        this.requestedBy = Objects.requireNonNull(requestedBy, "requestedBy must not be null");
        this.requestedAt = Instant.now();
        this.status = ApprovalStatus.PENDING;
    }

    public String getRequestId() { return requestId; }
    public String getEnvironment() { return environment; }
    public String getArtifactVersion() { return artifactVersion; }
    public String getRequestedBy() { return requestedBy; }
    public Instant getRequestedAt() { return requestedAt; }
    public ApprovalStatus getStatus() { return status; }
    public String getReviewedBy() { return reviewedBy; }
    public String getReviewNote() { return reviewNote; }

    public void approve(String reviewer, String note) {
        this.status = ApprovalStatus.APPROVED;
        this.reviewedBy = reviewer;
        this.reviewNote = note;
    }

    public void reject(String reviewer, String note) {
        this.status = ApprovalStatus.REJECTED;
        this.reviewedBy = reviewer;
        this.reviewNote = note;
    }

    public boolean isPending() {
        return status == ApprovalStatus.PENDING;
    }

    @Override
    public String toString() {
        return String.format("ApprovalRequest{id='%s', env='%s', version='%s', status=%s}",
                requestId, environment, artifactVersion, status);
    }
}
