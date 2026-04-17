package com.jarvis.deploy.report;

public class ExportResult {

    private final boolean successful;
    private final String content;
    private final DeploymentReportExporter.Format format;
    private final int entryCount;
    private final String errorMessage;

    private ExportResult(boolean successful, String content,
                         DeploymentReportExporter.Format format, int entryCount, String errorMessage) {
        this.successful   = successful;
        this.content      = content;
        this.format       = format;
        this.entryCount   = entryCount;
        this.errorMessage = errorMessage;
    }

    public static ExportResult success(String content, DeploymentReportExporter.Format format, int entryCount) {
        return new ExportResult(true, content, format, entryCount, null);
    }

    public static ExportResult failure(String errorMessage) {
        return new ExportResult(false, null, null, 0, errorMessage);
    }

    public boolean isSuccessful()  { return successful; }
    public String getContent()     { return content; }
    public DeploymentReportExporter.Format getFormat() { return format; }
    public int getEntryCount()     { return entryCount; }
    public String getErrorMessage(){ return errorMessage; }

    @Override
    public String toString() {
        return successful
            ? String.format("ExportResult{format=%s, entries=%d}", format, entryCount)
            : String.format("ExportResult{failed, reason=%s}", errorMessage);
    }
}
