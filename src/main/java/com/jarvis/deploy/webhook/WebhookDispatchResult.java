package com.jarvis.deploy.webhook;

import java.util.Objects;

/**
 * Encapsulates the result of a webhook dispatch operation.
 */
public class WebhookDispatchResult {

    public enum Status { SUCCESS, PARTIAL, FAILED, SKIPPED }

    private final String eventId;
    private final Status status;
    private final int successCount;
    private final int failureCount;

    private WebhookDispatchResult(String eventId, Status status, int successCount, int failureCount) {
        this.eventId = Objects.requireNonNull(eventId);
        this.status = Objects.requireNonNull(status);
        this.successCount = successCount;
        this.failureCount = failureCount;
    }

    public static WebhookDispatchResult skipped(String eventId) {
        return new WebhookDispatchResult(eventId, Status.SKIPPED, 0, 0);
    }

    public static WebhookDispatchResult of(String eventId, int successCount, int failureCount) {
        Status status;
        if (failureCount == 0) {
            status = Status.SUCCESS;
        } else if (successCount == 0) {
            status = Status.FAILED;
        } else {
            status = Status.PARTIAL;
        }
        return new WebhookDispatchResult(eventId, status, successCount, failureCount);
    }

    public String getEventId() { return eventId; }
    public Status getStatus() { return status; }
    public int getSuccessCount() { return successCount; }
    public int getFailureCount() { return failureCount; }
    public boolean isFullySuccessful() { return status == Status.SUCCESS; }
    public boolean isSkipped() { return status == Status.SKIPPED; }

    @Override
    public String toString() {
        return "WebhookDispatchResult{" +
                "eventId='" + eventId + '\'' +
                ", status=" + status +
                ", successCount=" + successCount +
                ", failureCount=" + failureCount +
                '}';
    }
}
