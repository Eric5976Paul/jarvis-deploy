package com.jarvis.deploy.promotion;

import java.time.Instant;

/**
 * Encapsulates the outcome of an environment promotion attempt.
 */
public class PromotionResult {

    public enum Status { SUCCESS, FAILED, SKIPPED }

    private final Status status;
    private final String message;
    private final PromotionRequest request;
    private final Instant timestamp;

    private PromotionResult(Status status, String message, PromotionRequest request) {
        this.status = status;
        this.message = message;
        this.request = request;
        this.timestamp = Instant.now();
    }

    public static PromotionResult success(PromotionRequest request, String message) {
        return new PromotionResult(Status.SUCCESS, message, request);
    }

    public static PromotionResult failed(PromotionRequest request, String reason) {
        return new PromotionResult(Status.FAILED, reason, request);
    }

    public static PromotionResult skipped(PromotionRequest request, String reason) {
        return new PromotionResult(Status.SKIPPED, reason, request);
    }

    public boolean isSuccessful() { return status == Status.SUCCESS; }
    public Status getStatus() { return status; }
    public String getMessage() { return message; }
    public PromotionRequest getRequest() { return request; }
    public Instant getTimestamp() { return timestamp; }

    @Override
    public String toString() {
        return String.format("PromotionResult{status=%s, message='%s', request=%s}",
                status, message, request);
    }
}
