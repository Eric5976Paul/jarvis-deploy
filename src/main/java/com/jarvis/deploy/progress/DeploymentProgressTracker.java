package com.jarvis.deploy.progress;

import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks real-time progress of an active deployment by recording named stages
 * and their completion percentages. Thread-safe for concurrent stage updates.
 */
public class DeploymentProgressTracker {

    private final String deploymentId;
    private final Instant startedAt;
    private final Map<String, DeploymentProgressStage> stages = new ConcurrentHashMap<>();
    private final Map<String, Integer> stageOrder = new LinkedHashMap<>();

    private volatile int overallPercent = 0;
    private volatile String currentStage = null;
    private volatile boolean completed = false;
    private volatile boolean failed = false;

    public DeploymentProgressTracker(String deploymentId) {
        if (deploymentId == null || deploymentId.isBlank()) {
            throw new IllegalArgumentException("deploymentId must not be blank");
        }
        this.deploymentId = deploymentId;
        this.startedAt = Instant.now();
    }

    public void registerStage(String stageName, int orderIndex) {
        stageOrder.put(stageName, orderIndex);
        stages.put(stageName, new DeploymentProgressStage(stageName, orderIndex));
    }

    public void beginStage(String stageName) {
        DeploymentProgressStage stage = getOrCreate(stageName);
        stage.begin();
        this.currentStage = stageName;
        recalculateOverall();
    }

    public void updateStageProgress(String stageName, int percent) {
        if (percent < 0 || percent > 100) {
            throw new IllegalArgumentException("percent must be between 0 and 100");
        }
        DeploymentProgressStage stage = getOrCreate(stageName);
        stage.setPercent(percent);
        recalculateOverall();
    }

    public void completeStage(String stageName) {
        DeploymentProgressStage stage = getOrCreate(stageName);
        stage.complete();
        recalculateOverall();
    }

    public void failStage(String stageName, String reason) {
        DeploymentProgressStage stage = getOrCreate(stageName);
        stage.fail(reason);
        this.failed = true;
        recalculateOverall();
    }

    public void markCompleted() {
        this.overallPercent = 100;
        this.completed = true;
    }

    public Optional<DeploymentProgressStage> getStage(String stageName) {
        return Optional.ofNullable(stages.get(stageName));
    }

    public Map<String, DeploymentProgressStage> getAllStages() {
        return Collections.unmodifiableMap(stages);
    }

    public String getDeploymentId() { return deploymentId; }
    public Instant getStartedAt()   { return startedAt; }
    public int getOverallPercent()  { return overallPercent; }
    public String getCurrentStage() { return currentStage; }
    public boolean isCompleted()    { return completed; }
    public boolean isFailed()       { return failed; }

    private DeploymentProgressStage getOrCreate(String name) {
        return stages.computeIfAbsent(name,
                n -> new DeploymentProgressStage(n, stageOrder.getOrDefault(n, 99)));
    }

    private void recalculateOverall() {
        if (stages.isEmpty()) return;
        int sum = stages.values().stream().mapToInt(DeploymentProgressStage::getPercent).sum();
        this.overallPercent = Math.min(100, sum / stages.size());
    }
}
