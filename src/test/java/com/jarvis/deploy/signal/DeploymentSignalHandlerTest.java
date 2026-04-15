package com.jarvis.deploy.signal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

class DeploymentSignalHandlerTest {

    private DeploymentSignalHandler handler;

    @BeforeEach
    void setUp() {
        handler = new DeploymentSignalHandler();
    }

    @Test
    void sendPause_shouldTransitionToPaused() {
        SignalResult result = handler.send("deploy-1", DeploymentSignal.PAUSE);
        assertTrue(result.isSuccess());
        assertEquals(SignalState.PAUSED, handler.getState("deploy-1"));
        assertTrue(handler.isPaused("deploy-1"));
    }

    @Test
    void sendResume_afterPause_shouldTransitionToRunning() {
        handler.send("deploy-2", DeploymentSignal.PAUSE);
        SignalResult result = handler.send("deploy-2", DeploymentSignal.RESUME);
        assertTrue(result.isSuccess());
        assertEquals(SignalState.RUNNING, handler.getState("deploy-2"));
        assertFalse(handler.isPaused("deploy-2"));
    }

    @Test
    void sendResume_whenNotPaused_shouldFail() {
        SignalResult result = handler.send("deploy-3", DeploymentSignal.RESUME);
        assertFalse(result.isSuccess());
        assertNotNull(result.getErrorMessage());
        assertTrue(result.getErrorMessage().contains("not paused"));
    }

    @Test
    void sendAbort_shouldTransitionToAborted() {
        SignalResult result = handler.send("deploy-4", DeploymentSignal.ABORT);
        assertTrue(result.isSuccess());
        assertEquals(SignalState.ABORTED, handler.getState("deploy-4"));
        assertTrue(handler.isAborted("deploy-4"));
    }

    @Test
    void sendSignal_toAbortedDeployment_shouldFail() {
        handler.send("deploy-5", DeploymentSignal.ABORT);
        SignalResult result = handler.send("deploy-5", DeploymentSignal.PAUSE);
        assertFalse(result.isSuccess());
        assertTrue(result.getErrorMessage().contains("aborted"));
    }

    @Test
    void sendSignal_withBlankId_shouldFail() {
        SignalResult result = handler.send("  ", DeploymentSignal.PAUSE);
        assertFalse(result.isSuccess());
        assertNotNull(result.getErrorMessage());
    }

    @Test
    void sendSignal_withNullSignal_shouldFail() {
        SignalResult result = handler.send("deploy-6", null);
        assertFalse(result.isSuccess());
    }

    @Test
    void registerCustomHandler_shouldBeInvokedOnSignal() {
        AtomicBoolean called = new AtomicBoolean(false);
        handler.registerHandler(DeploymentSignal.PAUSE, id -> called.set(true));
        handler.send("deploy-7", DeploymentSignal.PAUSE);
        assertTrue(called.get());
    }

    @Test
    void reset_shouldClearDeploymentState() {
        handler.send("deploy-8", DeploymentSignal.PAUSE);
        handler.reset("deploy-8");
        assertEquals(SignalState.RUNNING, handler.getState("deploy-8"));
    }

    @Test
    void getState_unknownDeployment_shouldReturnRunning() {
        assertEquals(SignalState.RUNNING, handler.getState("unknown"));
    }
}
