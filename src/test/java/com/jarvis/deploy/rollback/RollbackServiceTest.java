package com.jarvis.deploy.rollback;

import com.jarvis.deploy.deployment.DeploymentHistory;
import com.jarvis.deploy.deployment.DeploymentRecord;
import com.jarvis.deploy.deployment.DeploymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RollbackServiceTest {

    private DeploymentHistory deploymentHistory;
    private DeploymentService deploymentService;
    private RollbackService rollbackService;

    @BeforeEach
    void setUp() {
        deploymentHistory = Mockito.mock(DeploymentHistory.class);
        deploymentService = Mockito.mock(DeploymentService.class);
        rollbackService = new RollbackService(deploymentHistory, deploymentService);
    }

    @Test
    void rollback_shouldReturnSuccess_whenPreviousDeploymentExistsAndDeploySucceeds() {
        DeploymentRecord record = new DeploymentRecord("production", "1.2.0", "/artifacts/app-1.2.0.jar");
        when(deploymentHistory.getPreviousSuccessful("production")).thenReturn(Optional.of(record));
        when(deploymentService.deploy("production", "1.2.0", "/artifacts/app-1.2.0.jar")).thenReturn(true);

        RollbackResult result = rollbackService.rollback("production");

        assertTrue(result.isSuccessful());
        assertEquals("production", result.getEnvironment());
        assertEquals("1.2.0", result.getVersion());
        verify(deploymentService, times(1)).deploy("production", "1.2.0", "/artifacts/app-1.2.0.jar");
    }

    @Test
    void rollback_shouldReturnFailure_whenNoPreviousDeploymentExists() {
        when(deploymentHistory.getPreviousSuccessful("staging")).thenReturn(Optional.empty());

        RollbackResult result = rollbackService.rollback("staging");

        assertFalse(result.isSuccessful());
        assertEquals("staging", result.getEnvironment());
        assertNull(result.getVersion());
        verifyNoInteractions(deploymentService);
    }

    @Test
    void rollback_shouldReturnFailure_whenDeploymentFails() {
        DeploymentRecord record = new DeploymentRecord("production", "1.1.0", "/artifacts/app-1.1.0.jar");
        when(deploymentHistory.getPreviousSuccessful("production")).thenReturn(Optional.of(record));
        when(deploymentService.deploy("production", "1.1.0", "/artifacts/app-1.1.0.jar")).thenReturn(false);

        RollbackResult result = rollbackService.rollback("production");

        assertFalse(result.isSuccessful());
        assertEquals("production", result.getEnvironment());
    }

    @Test
    void rollback_shouldThrowException_whenEnvironmentIsBlank() {
        assertThrows(IllegalArgumentException.class, () -> rollbackService.rollback(""));
        assertThrows(IllegalArgumentException.class, () -> rollbackService.rollback(null));
    }
}
