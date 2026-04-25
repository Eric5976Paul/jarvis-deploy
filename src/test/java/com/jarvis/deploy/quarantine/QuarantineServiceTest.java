package com.jarvis.deploy.quarantine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class QuarantineServiceTest {

    private QuarantineService service;

    @BeforeEach
    void setUp() {
        service = new QuarantineService();
    }

    @Test
    void shouldQuarantineDeployment() {
        QuarantineEntry entry = service.quarantine("d1", "staging", QuarantineReason.HEALTH_CHECK_FAILED, "503 error");

        assertNotNull(entry);
        assertTrue(service.isQuarantined("d1"));
    }

    @Test
    void shouldReturnFalseForUnknownDeployment() {
        assertFalse(service.isQuarantined("unknown"));
    }

    @Test
    void shouldGetEntryByDeploymentId() {
        service.quarantine("d2", "prod", QuarantineReason.COMPLIANCE_VIOLATION, "policy breach");
        Optional<QuarantineEntry> entry = service.getEntry("d2");

        assertTrue(entry.isPresent());
        assertEquals(QuarantineReason.COMPLIANCE_VIOLATION, entry.get().getReason());
    }

    @Test
    void shouldReleaseQuarantinedDeployment() {
        service.quarantine("d3", "dev", QuarantineReason.MANUAL_OVERRIDE, "manual");
        assertTrue(service.isQuarantined("d3"));

        boolean released = service.release("d3");

        assertTrue(released);
        assertFalse(service.isQuarantined("d3"));
    }

    @Test
    void shouldReturnFalseWhenReleasingNonExistentEntry() {
        assertFalse(service.release("nonexistent"));
    }

    @Test
    void shouldReturnFalseWhenReleasingAlreadyReleasedEntry() {
        service.quarantine("d4", "staging", QuarantineReason.CANARY_EVALUATION_FAILED, "failed");
        service.release("d4");

        assertFalse(service.release("d4"));
    }

    @Test
    void shouldListActiveQuarantines() {
        service.quarantine("d5", "prod", QuarantineReason.SECURITY_SCAN_FAILED, "vuln found");
        service.quarantine("d6", "staging", QuarantineReason.HEALTH_CHECK_FAILED, "timeout");
        service.quarantine("d7", "dev", QuarantineReason.MANUAL_OVERRIDE, "test");
        service.release("d7");

        List<QuarantineEntry> active = service.getActiveQuarantines();
        assertEquals(2, active.size());
    }

    @Test
    void shouldFilterByEnvironment() {
        service.quarantine("d8", "prod", QuarantineReason.HEALTH_CHECK_FAILED, "err");
        service.quarantine("d9", "prod", QuarantineReason.COMPLIANCE_VIOLATION, "err");
        service.quarantine("d10", "staging", QuarantineReason.MANUAL_OVERRIDE, "err");

        List<QuarantineEntry> prodEntries = service.getQuarantinesByEnvironment("prod");
        assertEquals(2, prodEntries.size());
        assertTrue(prodEntries.stream().allMatch(e -> e.getEnvironment().equals("prod")));
    }

    @Test
    void shouldPurgeReleasedEntries() {
        service.quarantine("d11", "dev", QuarantineReason.MANUAL_OVERRIDE, "test");
        service.quarantine("d12", "dev", QuarantineReason.MANUAL_OVERRIDE, "test");
        service.release("d11");

        int purged = service.purgeReleased();

        assertEquals(1, purged);
        assertFalse(service.getEntry("d11").isPresent());
        assertTrue(service.getEntry("d12").isPresent());
    }
}
