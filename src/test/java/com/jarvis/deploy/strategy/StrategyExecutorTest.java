package com.jarvis.deploy.strategy;

import com.jarvis.deploy.deployment.DeploymentRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class StrategyExecutorTest {

    private DeploymentStrategyRegistry registry;
    private StrategyExecutor executor;
    private DeploymentStrategy mockStrategy;
    private DeploymentRecord mockRecord;

    @BeforeEach
    void setUp() {
        registry = new DeploymentStrategyRegistry();
        executor = new StrategyExecutor(registry);
        mockStrategy = mock(DeploymentStrategy.class);
        mockRecord = mock(DeploymentRecord.class);
    }

    @Test
    void shouldExecuteRegisteredStrategy() {
        StrategyResult expected = StrategyResult.success("rolling", "deployed");
        when(mockStrategy.execute(mockRecord)).thenReturn(expected);
        registry.register("rolling", mockStrategy);

        StrategyResult result = executor.execute("rolling", mockRecord);

        assertTrue(result.isSuccess());
        assertEquals("rolling", result.getStrategyName());
        verify(mockStrategy).execute(mockRecord);
    }

    @Test
    void shouldReturnFailureForUnknownStrategy() {
        StrategyResult result = executor.execute("nonexistent", mockRecord);
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("nonexistent"));
    }

    @Test
    void shouldReturnFailureWhenRecordIsNull() {
        registry.register("rolling", mockStrategy);
        StrategyResult result = executor.execute("rolling", null);
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("null"));
    }

    @Test
    void shouldHandleStrategyException() {
        when(mockStrategy.execute(mockRecord)).thenThrow(new RuntimeException("boom"));
        registry.register("canary", mockStrategy);

        StrategyResult result = executor.execute("canary", mockRecord);

        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("boom"));
    }

    @Test
    void shouldReportSupportCorrectly() {
        registry.register("blue-green", mockStrategy);
        assertTrue(executor.supports("blue-green"));
        assertFalse(executor.supports("unknown"));
    }

    @Test
    void shouldThrowWhenRegistryIsNull() {
        assertThrows(NullPointerException.class, () -> new StrategyExecutor(null));
    }
}
