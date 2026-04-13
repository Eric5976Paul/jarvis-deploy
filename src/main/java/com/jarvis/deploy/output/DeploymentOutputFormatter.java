package com.jarvis.deploy.output;

import com.jarvis.deploy.deployment.DeploymentRecord;
import com.jarvis.deploy.pipeline.PipelineResult;
import com.jarvis.deploy.rollback.RollbackResult;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Formats deployment-related objects into human-readable CLI output strings.
 */
public class DeploymentOutputFormatter {

    private static final DateTimeFormatter DISPLAY_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final OutputStyle style;

    public DeploymentOutputFormatter(OutputStyle style) {
        if (style == null) {
            throw new IllegalArgumentException("OutputStyle must not be null");
        }
        this.style = style;
    }

    public String formatRecord(DeploymentRecord record) {
        if (record == null) {
            return "[no record]";
        }
        String timestamp = record.getDeployedAt() != null
                ? record.getDeployedAt().format(DISPLAY_FORMAT)
                : "unknown";
        return String.format("[%s] env=%-12s version=%-15s status=%s",
                timestamp,
                record.getEnvironment(),
                record.getVersion(),
                record.getStatus());
    }

    public String formatHistory(List<DeploymentRecord> records) {
        if (records == null || records.isEmpty()) {
            return "No deployment history found.";
        }
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%-20s %-14s %-17s %s%n",
                "Timestamp", "Environment", "Version", "Status"));
        sb.append("-".repeat(70)).append("\n");
        for (DeploymentRecord r : records) {
            sb.append(formatRecord(r)).append("\n");
        }
        return sb.toString().trim();
    }

    public String formatPipelineResult(PipelineResult result) {
        if (result == null) {
            return "[no pipeline result]";
        }
        String status = result.isSuccess() ? "SUCCESS" : "FAILED";
        String base = String.format("Pipeline %s | env=%s version=%s",
                status, result.getEnvironment(), result.getVersion());
        if (!result.isSuccess() && result.getErrorMessage() != null) {
            base += " | error=" + result.getErrorMessage();
        }
        if (style == OutputStyle.VERBOSE && result.getDurationMs() > 0) {
            base += String.format(" | duration=%dms", result.getDurationMs());
        }
        return base;
    }

    public String formatRollbackResult(RollbackResult result) {
        if (result == null) {
            return "[no rollback result]";
        }
        String status = result.isSuccess() ? "SUCCESS" : "FAILED";
        String base = String.format("Rollback %s | env=%s rolledBackTo=%s",
                status, result.getEnvironment(), result.getRolledBackToVersion());
        if (!result.isSuccess() && result.getReason() != null) {
            base += " | reason=" + result.getReason();
        }
        return base;
    }

    public OutputStyle getStyle() {
        return style;
    }
}
