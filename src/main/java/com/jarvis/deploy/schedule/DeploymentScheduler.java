package com.jarvis.deploy.schedule;

import com.jarvis.deploy.pipeline.DeploymentPipeline;
import com.jarvis.deploy.pipeline.PipelineRequest;
import com.jarvis.deploy.pipeline.PipelineResult;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Manages scheduled deployments, allowing deployments to be queued
 * for future execution at a specified delay or fixed time.
 */
public class DeploymentScheduler {

    private final DeploymentPipeline pipeline;
    private final ScheduledExecutorService executor;
    private final Map<String, ScheduledFuture<?>> scheduledJobs;
    private final List<ScheduleRecord> history;

    public DeploymentScheduler(DeploymentPipeline pipeline) {
        this(pipeline, Executors.newScheduledThreadPool(4));
    }

    public DeploymentScheduler(DeploymentPipeline pipeline, ScheduledExecutorService executor) {
        this.pipeline = pipeline;
        this.executor = executor;
        this.scheduledJobs = new ConcurrentHashMap<>();
        this.history = Collections.synchronizedList(new ArrayList<>());
    }

    public ScheduleResult schedule(PipelineRequest request, long delaySeconds) {
        if (request == null) {
            return ScheduleResult.failure("PipelineRequest must not be null");
        }
        if (delaySeconds < 0) {
            return ScheduleResult.failure("Delay must be non-negative");
        }

        String jobId = request.getEnvironment() + "-" + request.getArtifactId() + "-" + Instant.now().toEpochMilli();

        ScheduledFuture<?> future = executor.schedule(() -> {
            PipelineResult result = pipeline.execute(request);
            history.add(new ScheduleRecord(jobId, request, result, Instant.now()));
            scheduledJobs.remove(jobId);
        }, delaySeconds, TimeUnit.SECONDS);

        scheduledJobs.put(jobId, future);
        return ScheduleResult.success(jobId, Instant.now().plusSeconds(delaySeconds));
    }

    public boolean cancel(String jobId) {
        ScheduledFuture<?> future = scheduledJobs.remove(jobId);
        if (future != null && !future.isDone()) {
            return future.cancel(false);
        }
        return false;
    }

    public List<String> getPendingJobIds() {
        return new ArrayList<>(scheduledJobs.keySet());
    }

    public List<ScheduleRecord> getHistory() {
        return Collections.unmodifiableList(history);
    }

    public void shutdown() {
        executor.shutdown();
    }
}
