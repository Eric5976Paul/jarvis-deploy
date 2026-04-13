package com.jarvis.deploy.lock;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class DeploymentLockManagerTest {

    @TempDir
    Path tempDir;

    private DeploymentLockManager lockManager;

    @BeforeEach
    void setUp() {
        lockManager = new DeploymentLockManager(tempDir.resolve("locks").toString());
    }

    @AfterEach
    void tearDown() {
        lockManager.releaseLock("staging");
        lockManager.releaseLock("production");
    }

    @Test
    void acquireLock_shouldSucceedForFreeEnvironment() {
        LockAcquisitionResult result = lockManager.acquireLock("staging");
        assertTrue(result.isSuccess());
        assertEquals("staging", result.getEnvironment());
    }

    @Test
    void acquireLock_shouldFailForAlreadyLockedEnvironment() {
        lockManager.acquireLock("staging");
        LockAcquisitionResult result = lockManager.acquireLock("staging");
        assertFalse(result.isSuccess());
        assertNotNull(result.getErrorMessage());
        assertTrue(result.getErrorMessage().contains("staging"));
    }

    @Test
    void acquireLock_shouldFailForNullEnvironment() {
        LockAcquisitionResult result = lockManager.acquireLock(null);
        assertFalse(result.isSuccess());
        assertNotNull(result.getErrorMessage());
    }

    @Test
    void acquireLock_shouldFailForBlankEnvironment() {
        LockAcquisitionResult result = lockManager.acquireLock("   ");
        assertFalse(result.isSuccess());
    }

    @Test
    void releaseLock_shouldReturnTrueAfterSuccessfulRelease() {
        lockManager.acquireLock("staging");
        boolean released = lockManager.releaseLock("staging");
        assertTrue(released);
    }

    @Test
    void releaseLock_shouldReturnFalseIfNoLockExists() {
        boolean released = lockManager.releaseLock("production");
        assertFalse(released);
    }

    @Test
    void isLocked_shouldReturnTrueWhenLockHeld() {
        lockManager.acquireLock("production");
        assertTrue(lockManager.isLocked("production"));
    }

    @Test
    void isLocked_shouldReturnFalseWhenNotLocked() {
        assertFalse(lockManager.isLocked("staging"));
    }

    @Test
    void acquireLock_shouldSucceedAfterRelease() {
        lockManager.acquireLock("staging");
        lockManager.releaseLock("staging");
        LockAcquisitionResult result = lockManager.acquireLock("staging");
        assertTrue(result.isSuccess());
    }

    @Test
    void multipleDifferentEnvironments_canBeLocked_simultaneously() {
        LockAcquisitionResult stagingResult = lockManager.acquireLock("staging");
        LockAcquisitionResult prodResult = lockManager.acquireLock("production");
        assertTrue(stagingResult.isSuccess());
        assertTrue(prodResult.isSuccess());
    }
}
