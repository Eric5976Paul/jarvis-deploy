package com.jarvis.deploy.delegation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DeploymentDelegationServiceTest {

    private DeploymentDelegationService service;

    @BeforeEach
    void setUp() {
        service = new DeploymentDelegationService();
    }

    @Test
    void delegate_validRequest_returnsSuccess() {
        DelegationRequest req = new DelegationRequest("alice", "bob",
                List.of("staging"), Instant.now().plusSeconds(3600));
        DelegationResult result = service.delegate(req);
        assertTrue(result.isSuccess());
        assertNotNull(result.getDelegationId());
    }

    @Test
    void delegate_nullRequest_returnsFailure() {
        DelegationResult result = service.delegate(null);
        assertFalse(result.isSuccess());
        assertNotNull(result.getErrorMessage());
    }

    @Test
    void delegate_samePrincipal_returnsFailure() {
        DelegationRequest req = new DelegationRequest("alice", "alice",
                null, Instant.now().plusSeconds(3600));
        DelegationResult result = service.delegate(req);
        assertFalse(result.isSuccess());
    }

    @Test
    void delegate_expiredTime_returnsFailure() {
        DelegationRequest req = new DelegationRequest("alice", "bob",
                null, Instant.now().minusSeconds(10));
        DelegationResult result = service.delegate(req);
        assertFalse(result.isSuccess());
    }

    @Test
    void isAuthorized_withMatchingEnv_returnsTrue() {
        DelegationRequest req = new DelegationRequest("alice", "bob",
                List.of("prod"), Instant.now().plusSeconds(3600));
        service.delegate(req);
        assertTrue(service.isAuthorized("alice", "bob", "prod"));
    }

    @Test
    void isAuthorized_withNonMatchingEnv_returnsFalse() {
        DelegationRequest req = new DelegationRequest("alice", "bob",
                List.of("staging"), Instant.now().plusSeconds(3600));
        service.delegate(req);
        assertFalse(service.isAuthorized("alice", "bob", "prod"));
    }

    @Test
    void isAuthorized_noEnvRestriction_returnsTrueForAnyEnv() {
        DelegationRequest req = new DelegationRequest("alice", "bob",
                null, Instant.now().plusSeconds(3600));
        service.delegate(req);
        assertTrue(service.isAuthorized("alice", "bob", "prod"));
        assertTrue(service.isAuthorized("alice", "bob", "dev"));
    }

    @Test
    void isAuthorized_noDelegation_returnsFalse() {
        assertFalse(service.isAuthorized("alice", "charlie", "prod"));
    }

    @Test
    void revoke_existingDelegation_returnsTrue() {
        DelegationRequest req = new DelegationRequest("alice", "bob",
                null, Instant.now().plusSeconds(3600));
        service.delegate(req);
        assertTrue(service.revoke("alice", "bob"));
        assertFalse(service.isAuthorized("alice", "bob", "prod"));
    }

    @Test
    void revoke_nonExistentDelegation_returnsFalse() {
        assertFalse(service.revoke("alice", "nobody"));
    }

    @Test
    void listActive_returnsDelegations() {
        service.delegate(new DelegationRequest("alice", "bob", null, Instant.now().plusSeconds(3600)));
        service.delegate(new DelegationRequest("carol", "dave", List.of("staging"), Instant.now().plusSeconds(7200)));
        List<DelegationRecord> active = service.listActive();
        assertEquals(2, active.size());
    }

    @Test
    void listActive_excludesExpiredDelegations() throws InterruptedException {
        service.delegate(new DelegationRequest("alice", "bob", null, Instant.now().plusMillis(50)));
        Thread.sleep(100);
        List<DelegationRecord> active = service.listActive();
        assertTrue(active.isEmpty());
    }
}
