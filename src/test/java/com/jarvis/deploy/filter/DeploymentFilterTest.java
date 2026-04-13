package com.jarvis.deploy.filter;

import com.jarvis.deploy.deployment.DeploymentRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DeploymentFilterTest {

    private DeploymentRecord recProd;
    private DeploymentRecord recStaging;
    private DeploymentRecord recOld;
    private List<DeploymentRecord> allRecords;

    @BeforeEach
    void setUp() {
        Instant now = Instant.now();

        recProd = new DeploymentRecord("prod", "1.2.0", "SUCCESS", now);
        recStaging = new DeploymentRecord("staging", "1.2.0", "SUCCESS", now.minusSeconds(3600));
        recOld = new DeploymentRecord("prod", "1.1.0", "FAILED", now.minusSeconds(86400));

        allRecords = Arrays.asList(recProd, recStaging, recOld);
    }

    @Test
    void filterByEnvironment_returnsMatchingRecords() {
        DeploymentFilter filter = DeploymentFilter.builder()
                .environment("prod")
                .build();

        List<DeploymentRecord> result = filter.apply(allRecords);

        assertEquals(2, result.size());
        assertTrue(result.contains(recProd));
        assertTrue(result.contains(recOld));
    }

    @Test
    void filterByStatus_returnsOnlyMatchingStatus() {
        DeploymentFilter filter = DeploymentFilter.builder()
                .status("FAILED")
                .build();

        List<DeploymentRecord> result = filter.apply(allRecords);

        assertEquals(1, result.size());
        assertEquals(recOld, result.get(0));
    }

    @Test
    void filterByVersion_returnsMatchingVersion() {
        DeploymentFilter filter = DeploymentFilter.builder()
                .version("1.2.0")
                .build();

        List<DeploymentRecord> result = filter.apply(allRecords);

        assertEquals(2, result.size());
        assertTrue(result.contains(recProd));
        assertTrue(result.contains(recStaging));
    }

    @Test
    void filterByTimeRange_returnsRecordsWithinRange() {
        Instant from = Instant.now().minusSeconds(7200);
        Instant to = Instant.now();

        DeploymentFilter filter = DeploymentFilter.builder()
                .from(from)
                .to(to)
                .build();

        List<DeploymentRecord> result = filter.apply(allRecords);

        assertEquals(2, result.size());
        assertFalse(result.contains(recOld));
    }

    @Test
    void filterWithCombinedCriteria_returnsNarrowedResults() {
        DeploymentFilter filter = DeploymentFilter.builder()
                .environment("prod")
                .status("SUCCESS")
                .build();

        List<DeploymentRecord> result = filter.apply(allRecords);

        assertEquals(1, result.size());
        assertEquals(recProd, result.get(0));
    }

    @Test
    void filterOnEmptyList_returnsEmptyList() {
        DeploymentFilter filter = DeploymentFilter.builder()
                .environment("prod")
                .build();

        List<DeploymentRecord> result = filter.apply(List.of());

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void filterWithNoConditions_returnsAllRecords() {
        DeploymentFilter filter = DeploymentFilter.builder().build();

        List<DeploymentRecord> result = filter.apply(allRecords);

        assertEquals(3, result.size());
    }
}
