package com.jarvis.deploy.signal;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.logging.Logger;

/**
 * Handles deployment lifecycle signals (pause, resume, abort, drain)
 * allowing external processes to send control signals to running deployments.
 */
public class DeploymentSignalHandler {

    private static final Logger logger = Logger.getLogger(DeploymentSignalHandler.class.getName());

    private final Map<String, SignalState> deploymentStates = new ConcurrentHashMap<>();
    private final Map<DeploymentSignal, Consumer<String>> signalHandlers = new ConcurrentHashMap<>();

    public DeploymentSignalHandler() {
        registerDefaultHandlers();
    }

    private void registerDefaultHandlers() {
        signalHandlers.put(DeploymentSignal.PAUSE, id -> {
            deploymentStates.put(id, SignalState.PAUSED);
            logger.info("Deployment paused: " + id);
        });
        signalHandlers.put(DeploymentSignal.RESUME, id -> {
            deploymentStates.put(id, SignalState.RUNNING);
            logger.info("Deployment resumed: " + id);
        });
        signalHandlers.put(DeploymentSignal.ABORT, id -> {
            deploymentStates.put(id, SignalState.ABORTED);
            logger.warning("Deployment aborted: " + id);
        });
    }

    public SignalResult send(String deploymentId, DeploymentSignal signal) {
        if (deploymentId == null || deploymentId.isBlank()) {
            return SignalResult.failure(deploymentId, signal, "Deployment ID must not be blank");
        }
        if (signal == null) {
            return SignalResult.failure(deploymentId, null, "Signal must not be null");
        }

        SignalState currentState = deploymentStates.getOrDefault(deploymentId, SignalState.RUNNING);

        if (signal == DeploymentSignal.RESUME && currentState != SignalState.PAUSED) {
            return SignalResult.failure(deploymentId, signal,
                    "Cannot resume deployment that is not paused (current state: " + currentState + ")");
        }
        if (currentState == SignalState.ABORTED) {
            return SignalResult.failure(deploymentId, signal,
                    "Cannot send signal to aborted deployment");
        }

        Consumer<String> handler = signalHandlers.get(signal);
        if (handler != null) {
            handler.accept(deploymentId);
        }

        return SignalResult.success(deploymentId, signal, deploymentStates.get(deploymentId));
    }

    public SignalState getState(String deploymentId) {
        return deploymentStates.getOrDefault(deploymentId, SignalState.RUNNING);
    }

    public boolean isPaused(String deploymentId) {
        return SignalState.PAUSED == getState(deploymentId);
    }

    public boolean isAborted(String deploymentId) {
        return SignalState.ABORTED == getState(deploymentId);
    }

    public void registerHandler(DeploymentSignal signal, Consumer<String> handler) {
        if (signal != null && handler != null) {
            signalHandlers.put(signal, handler);
        }
    }

    public void reset(String deploymentId) {
        deploymentStates.remove(deploymentId);
    }
}
