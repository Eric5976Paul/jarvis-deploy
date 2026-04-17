package com.jarvis.deploy.report;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;

public class DeploymentReportExporter {

    public enum Format { JSON, CSV, TEXT }

    public ExportResult export(List<ReportEntry> entries, Format format) {
        if (entries == null || entries.isEmpty()) {
            return ExportResult.failure("No entries to export");
        }
        try {
            String content = switch (format) {
                case JSON -> toJson(entries);
                case CSV  -> toCsv(entries);
                case TEXT -> toText(entries);
            };
            return ExportResult.success(content, format, entries.size());
        } catch (Exception e) {
            return ExportResult.failure("Export failed: " + e.getMessage());
        }
    }

    private String toJson(List<ReportEntry> entries) {
        StringBuilder sb = new StringBuilder("[\n");
        for (int i = 0; i < entries.size(); i++) {
            ReportEntry e = entries.get(i);
            sb.append(String.format(
                "  {\"id\":\"%s\",\"env\":\"%s\",\"version\":\"%s\",\"status\":\"%s\",\"timestamp\":\"%s\"}",
                e.deploymentId(), e.environment(), e.version(), e.status(), e.timestamp()));
            if (i < entries.size() - 1) sb.append(",");
            sb.append("\n");
        }
        return sb.append("]").toString();
    }

    private String toCsv(List<ReportEntry> entries) {
        StringBuilder sb = new StringBuilder("deploymentId,environment,version,status,timestamp\n");
        for (ReportEntry e : entries) {
            sb.append(String.format("%s,%s,%s,%s,%s\n",
                e.deploymentId(), e.environment(), e.version(), e.status(), e.timestamp()));
        }
        return sb.toString();
    }

    private String toText(List<ReportEntry> entries) {
        StringBuilder sb = new StringBuilder("=== Deployment Report ===\n");
        for (ReportEntry e : entries) {
            sb.append(String.format("[%s] %s @ %s -> %s (%s)\n",
                e.status(), e.deploymentId(), e.environment(), e.version(), e.timestamp()));
        }
        return sb.toString();
    }
}
