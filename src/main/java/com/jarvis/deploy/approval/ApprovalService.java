package com.jarvis.deploy.approval;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages approval requests for gated deployment environments.
 * Tracks pending approvals and enforces approval gates before deployments proceed.
 */
public class ApprovalService {

    private final Map<String, ApprovalRequest> requests = new ConcurrentHashMap<>();
    private final Set<String> gatedEnvironments;

    public ApprovalService(Set<String> gatedEnvironments) {
        this.gatedEnvironments = Objects.requireNonNull(gatedEnvironments, "gatedEnvironments must not be null");
    }

    /**
     * Submits a new approval request. Returns the created request.
     * Throws if the environment is not gated.
     */
    public ApprovalRequest submit(String environment, String artifactVersion, String requestedBy) {
        if (!gatedEnvironments.contains(environment)) {
            throw new IllegalArgumentException("Environment '" + environment + "' does not require approval");
        }
        String id = UUID.randomUUID().toString();
        ApprovalRequest request = new ApprovalRequest(id, environment, artifactVersion, requestedBy);
        requests.put(id, request);
        return request;
    }

    /**
     * Approves a pending request by ID.
     */
    public ApprovalRequest approve(String requestId, String reviewer, String note) {
        ApprovalRequest request = getOrThrow(requestId);
        if (!request.isPending()) {
            throw new IllegalStateException("Request '" + requestId + "' is not in PENDING state");
        }
        request.approve(reviewer, note);
        return request;
    }

    /**
     * Rejects a pending request by ID.
     */
    public ApprovalRequest reject(String requestId, String reviewer, String note) {
        ApprovalRequest request = getOrThrow(requestId);
        if (!request.isPending()) {
            throw new IllegalStateException("Request '" + requestId + "' is not in PENDING state");
        }
        request.reject(reviewer, note);
        return request;
    }

    /**
     * Returns whether a given environment requires an approval gate.
     */
    public boolean requiresApproval(String environment) {
        return gatedEnvironments.contains(environment);
    }

    public Optional<ApprovalRequest> findById(String requestId) {
        return Optional.ofNullable(requests.get(requestId));
    }

    public List<ApprovalRequest> listPending() {
        List<ApprovalRequest> pending = new ArrayList<>();
        for (ApprovalRequest r : requests.values()) {
            if (r.isPending()) pending.add(r);
        }
        return Collections.unmodifiableList(pending);
    }

    private ApprovalRequest getOrThrow(String requestId) {
        ApprovalRequest request = requests.get(requestId);
        if (request == null) {
            throw new NoSuchElementException("No approval request found with id: " + requestId);
        }
        return request;
    }
}
