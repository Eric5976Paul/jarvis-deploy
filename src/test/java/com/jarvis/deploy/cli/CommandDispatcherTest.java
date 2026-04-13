package com.jarvis.deploy.cli;

import com.jarvis.deploy.config.ConfigLoader;
import com.jarvis.deploy.config.DeploymentConfig;
import com.jarvis.deploy.deployment.DeploymentService;
import com.jarvis.deploy.health.HealthCheckResult;
import com.jarvis.deploy.health.HealthChecker;
import com.jarvis.deploy.rollback.RollbackResult;
import com.jarvis.deploy.rollback.RollbackService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

class CommandDispatcherTest {

    private ConfigLoader configLoader;
    private DeploymentService deploymentService;
    private RollbackService rollbackService;
    private HealthChecker healthChecker;
    private CommandDispatcher dispatcher;

    @BeforeEach
    void setUp() {
        configLoader = Mockito.mock(ConfigLoader.class);
        deploymentService = Mockito.mock(DeploymentService.class);
        rollbackService = Mockito.mock(RollbackService.class);
        healthChecker = Mockito.mock(HealthChecker.class);
        dispatcher = new CommandDispatcher(configLoader, deploymentService, rollbackService, healthChecker);

        when(configLoader.load()).thenReturn(new DeploymentConfig());
    }

    @Test
    void dispatch_noArgs_returnsFailure() {
        CliResult result = dispatcher.dispatch(new String[]{});
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("No command provided"));
    }

    @Test
    void dispatch_unknownCommand_returnsFailure() {
        CliResult result = dispatcher.dispatch(new String[]{"unknown"});
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("Unknown command"));
    }

    @Test
    void dispatch_deployMissingArgs_returnsFailure() {
        CliResult result = dispatcher.dispatch(new String[]{"deploy", "production"});
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("Usage: deploy"));
    }

    @Test
    void dispatch_deploySuccess_returnsSuccess() {
        CliResult result = dispatcher.dispatch(new String[]{"deploy", "production", "1.2.3"});
        assertTrue(result.isSuccess());
        assertTrue(result.getMessage().contains("1.2.3"));
        assertTrue(result.getMessage().contains("production"));
    }

    @Test
    void dispatch_rollbackSuccess() {
        when(rollbackService.rollback("staging")).thenReturn(RollbackResult.success("Rolled back staging"));
        CliResult result = dispatcher.dispatch(new String[]{"rollback", "staging"});
        assertTrue(result.isSuccess());
    }

    @Test
    void dispatch_rollbackFailure() {
        when(rollbackService.rollback("staging")).thenReturn(RollbackResult.failure("No previous version"));
        CliResult result = dispatcher.dispatch(new String[]{"rollback", "staging"});
        assertFalse(result.isSuccess());
    }

    @Test
    void dispatch_healthCheck_healthy() {
        when(healthChecker.check(eq("production"), any()))
                .thenReturn(HealthCheckResult.healthy("All systems go"));
        CliResult result = dispatcher.dispatch(new String[]{"health", "production"});
        assertTrue(result.isSuccess());
        assertTrue(result.getMessage().contains("Health OK"));
    }

    @Test
    void dispatch_historyMissingEnv_returnsFailure() {
        CliResult result = dispatcher.dispatch(new String[]{"history"});
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("Usage: history"));
    }
}
