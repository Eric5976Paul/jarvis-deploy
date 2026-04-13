package com.jarvis.deploy.approval;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ApprovalServiceTest {

    private ApprovalService approvalService;

    @BeforeEach
    void setUp() {
        approvalService = new ApprovalService(Set.of("production", "staging"));
    }

    @Test
    void submit_createsApprovalRequestInPendingState() {
        ApprovalRequest request = approvalService.submit("production", "1.2.3", "alice");

        assertNotNull(request.getRequestId());
        assertEquals("production", request.getEnvironment());
        assertEquals("1.2.3", request.getArtifactVersion());
        assertEquals("alice", request.getRequestedBy());
        assertEquals(ApprovalStatus.PENDING, request.getStatus());
        assertTrue(request.isPending());
    }

    @Test
    void submit_throwsForNonGatedEnvironment() {
        assertThrows(IllegalArgumentException.class,
                () -> approvalService.submit("dev", "1.0.0", "bob"));
    }

    @Test
    void approve_updatesStatusToApproved() {
        ApprovalRequest request = approvalService.submit("staging", "2.0.0", "carol");

        ApprovalRequest approved = approvalService.approve(request.getRequestId(), "manager", "LGTM");

        assertEquals(ApprovalStatus.APPROVED, approved.getStatus());
        assertEquals("manager", approved.getReviewedBy());
        assertEquals("LGTM", approved.getReviewNote());
        assertFalse(approved.isPending());
    }

    @Test
    void reject_updatesStatusToRejected() {
        ApprovalRequest request = approvalService.submit("production", "3.1.0", "dave");

        ApprovalRequest rejected = approvalService.reject(request.getRequestId(), "lead", "Not ready");

        assertEquals(ApprovalStatus.REJECTED, rejected.getStatus());
        assertEquals("lead", rejected.getReviewedBy());
        assertEquals("Not ready", rejected.getReviewNote());
    }

    @Test
    void approve_throwsIfAlreadyApproved() {
        ApprovalRequest request = approvalService.submit("staging", "1.0.0", "eve");
        approvalService.approve(request.getRequestId(), "reviewer", "OK");

        assertThrows(IllegalStateException.class,
                () -> approvalService.approve(request.getRequestId(), "reviewer2", "Again"));
    }

    @Test
    void approve_throwsForUnknownRequestId() {
        assertThrows(NoSuchElementException.class,
                () -> approvalService.approve("nonexistent-id", "reviewer", "note"));
    }

    @Test
    void listPending_returnsOnlyPendingRequests() {
        ApprovalRequest r1 = approvalService.submit("production", "1.0.0", "user1");
        ApprovalRequest r2 = approvalService.submit("staging", "2.0.0", "user2");
        approvalService.approve(r1.getRequestId(), "admin", "approved");

        List<ApprovalRequest> pending = approvalService.listPending();

        assertEquals(1, pending.size());
        assertEquals(r2.getRequestId(), pending.get(0).getRequestId());
    }

    @Test
    void requiresApproval_returnsTrueForGatedEnvironments() {
        assertTrue(approvalService.requiresApproval("production"));
        assertTrue(approvalService.requiresApproval("staging"));
        assertFalse(approvalService.requiresApproval("dev"));
    }

    @Test
    void findById_returnsEmptyForMissingRequest() {
        assertTrue(approvalService.findById("missing").isEmpty());
    }
}
