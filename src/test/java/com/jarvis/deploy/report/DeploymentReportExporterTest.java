package com.jarvis.deploy.report;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DeploymentReportExporterTest {

    private DeploymentReportExporter exporter;
    private List<ReportEntry> entries;

    @BeforeEach
    void setUp() {
        exporter = new DeploymentReportExporter();
        entries = List.of(
            ReportEntry.of("dep-001", "staging", "1.2.0", "SUCCESS"),
            ReportEntry.of("dep-002", "production", "1.1.9", "FAILED")
        );
    }

    @Test
    void exportJson_returnsSuccessWithJsonContent() {
        ExportResult result = exporter.export(entries, DeploymentReportExporter.Format.JSON);
        assertTrue(result.isSuccessful());
        assertEquals(2, result.getEntryCount());
        assertNotNull(result.getContent());
        assertTrue(result.getContent().contains("dep-001"));
        assertTrue(result.getContent().contains("dep-002"));
        assertTrue(result.getContent().startsWith("["));
    }

    @Test
    void exportCsv_containsHeaderAndRows() {
        ExportResult result = exporter.export(entries, DeploymentReportExporter.Format.CSV);
        assertTrue(result.isSuccessful());
        assertTrue(result.getContent().startsWith("deploymentId,"));
        assertTrue(result.getContent().contains("staging"));
        assertTrue(result.getContent().contains("production"));
    }

    @Test
    void exportText_containsFormattedLines() {
        ExportResult result = exporter.export(entries, DeploymentReportExporter.Format.TEXT);
        assertTrue(result.isSuccessful());
        assertTrue(result.getContent().contains("=== Deployment Report ==="));
        assertTrue(result.getContent().contains("SUCCESS"));
        assertTrue(result.getContent().contains("FAILED"));
    }

    @Test
    void export_emptyList_returnsFailure() {
        ExportResult result = exporter.export(List.of(), DeploymentReportExporter.Format.JSON);
        assertFalse(result.isSuccessful());
        assertNotNull(result.getErrorMessage());
        assertNull(result.getContent());
    }

    @Test
    void export_nullList_returnsFailure() {
        ExportResult result = exporter.export(null, DeploymentReportExporter.Format.CSV);
        assertFalse(result.isSuccessful());
    }

    @Test
    void exportResult_toStringSuccessful() {
        ExportResult result = exporter.export(entries, DeploymentReportExporter.Format.TEXT);
        assertTrue(result.toString().contains("TEXT"));
    }
}
