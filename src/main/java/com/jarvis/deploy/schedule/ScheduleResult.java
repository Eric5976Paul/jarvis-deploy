package com.jarvis.deploy.schedule;

import java.time.Instant;

/**
 * Represents the outcome of a schedule request, including the job ID
 * and the estimated execution time on success.
 */
public class ScheduleResult {

    private final boolean success;
    private final String jobId;
    private final Instant scheduledAt;
    private final String errorMessage;

    private ScheduleResult(boolean success, String jobId, Instant scheduledAt, String errorMessage) {
        this.success = success;
        this.jobId = jobId;
        this.scheduledAt = scheduledAt;
        this.errorMessage = errorMessage;
    }

    public static ScheduleResult success(String jobId, Instant scheduledAt) {
        return new ScheduleResult(true, jobId, scheduledAt, null);
    }

    public static ScheduleResult failure(String errorMessage) {
        return new ScheduleResult(false, null, null, errorMessage);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getJobId() {
        return jobId;
    }

    public Instant getScheduledAt() {
        return scheduledAt;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    @Override
    public String toString() {
        if (success) {
            return "ScheduleResult{success=true, jobId='" + jobId + "', scheduledAt=" + scheduledAt + "}";
        }
        return "ScheduleResult{success=false, error='" + errorMessage + "'}";
    }
}
