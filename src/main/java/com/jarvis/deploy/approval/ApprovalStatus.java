package com.jarvis.deploy.approval;

/**
 * Lifecycle states for a deployment approval request.
 */
public enum ApprovalStatus {
    PENDING,
    APPROVED,
    REJECTED,
    EXPIRED
}
