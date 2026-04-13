package com.jarvis.deploy.output;

import com.jarvis.deploy.deployment.DeploymentRecord;
import com.jarvis.deploy.pipeline.PipelineResult;
import com.jarvis.deploy.rollback.RollbackResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DeploymentOutputFormatterTest {

    private DeploymentOutputFormatter formatter;
    private DeploymentOutputFormatter verboseFormatter;

    @BeforeEach
    void setUp() {
        formatter = new DeploymentOutputFormatter(OutputStyle.NORMAL);
        verboseFormatter = new DeploymentOutputFormatter(OutputStyle.VERBOSE);
    }

    @Test
    void constructor_nullStyle_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> new DeploymentOutputFormatter(null));
    }

    @Test
    void formatRecord_nullRecord_returnsPlaceholder() {
        assertEquals("[no record]", formatter.formatRecord(null));
    }

    @Test
    void formatRecord_validRecord_containsExpectedFields() {
        DeploymentRecord record = mock(DeploymentRecord.class);
        when(record.getDeployedAt()).thenReturn(LocalDateTime.of(2024, 6, 15, 10, 30, 0));
        when(record.getEnvironment()).thenReturn("production");
        when(record.getVersion()).thenReturn("1.4.2");
        when(record.getStatus()).thenReturn("SUCCESS");

        String output = formatter.formatRecord(record);

        assertTrue(output.contains("2024-06-15 10:30:00"));
        assertTrue(output.contains("production"));
        assertTrue(output.contains("1.4.2"));
        assertTrue(output.contains("SUCCESS"));
    }

    @Test
    void formatHistory_emptyList_returnsNoHistoryMessage() {
        String output = formatter.formatHistory(Collections.emptyList());
        assertEquals("No deployment history found.", output);
    }

    @Test
    void formatHistory_withRecords_containsHeaderAndRows() {
        DeploymentRecord record = mock(DeploymentRecord.class);
        when(record.getDeployedAt()).thenReturn(LocalDateTime.now());
        when(record.getEnvironment()).thenReturn("staging");
        when(record.getVersion()).thenReturn("2.0.0");
        when(record.getStatus()).thenReturn("SUCCESS");

        String output = formatter.formatHistory(List.of(record));

        assertTrue(output.contains("Timestamp"));
        assertTrue(output.contains("staging"));
        assertTrue(output.contains("2.0.0"));
    }

    @Test
    void formatPipelineResult_success_noErrorInOutput() {
        PipelineResult result = mock(PipelineResult.class);
        when(result.isSuccess()).thenReturn(true);
        when(result.getEnvironment()).thenReturn("dev");
        when(result.getVersion()).thenReturn("1.0.0");
        when(result.getDurationMs()).thenReturn(350L);

        String output = verboseFormatter.formatPipelineResult(result);

        assertTrue(output.contains("SUCCESS"));
        assertTrue(output.contains("350ms"));
        assertFalse(output.contains("error="));
    }

    @Test
    void formatPipelineResult_failure_includesErrorMessage() {
        PipelineResult result = mock(PipelineResult.class);
        when(result.isSuccess()).thenReturn(false);
        when(result.getEnvironment()).thenReturn("prod");
        when(result.getVersion()).thenReturn("3.1.0");
        when(result.getErrorMessage()).thenReturn("health check failed");

        String output = formatter.formatPipelineResult(result);

        assertTrue(output.contains("FAILED"));
        assertTrue(output.contains("health check failed"));
    }

    @Test
    void formatRollbackResult_success_containsVersionInfo() {
        RollbackResult result = mock(RollbackResult.class);
        when(result.isSuccess()).thenReturn(true);
        when(result.getEnvironment()).thenReturn("staging");
        when(result.getRolledBackToVersion()).thenReturn("1.9.0");

        String output = formatter.formatRollbackResult(result);

        assertTrue(output.contains("SUCCESS"));
        assertTrue(output.contains("1.9.0"));
    }

    @Test
    void getStyle_returnsConfiguredStyle() {
        assertEquals(OutputStyle.NORMAL, formatter.getStyle());
        assertEquals(OutputStyle.VERBOSE, verboseFormatter.getStyle());
    }
}
