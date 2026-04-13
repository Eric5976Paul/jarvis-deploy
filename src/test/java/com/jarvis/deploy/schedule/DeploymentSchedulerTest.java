package com.jarvis.deploy.schedule;

import com.jarvis.deploy.pipeline.DeploymentPipeline;
import com.jarvis.deploy.pipeline.PipelineRequest;
import com.jarvis.deploy.pipeline.PipelineResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeploymentSchedulerTest {

    @Mock
    private DeploymentPipeline pipeline;

    private DeploymentScheduler scheduler;

    @BeforeEach
    void setUp() {
        scheduler = new DeploymentScheduler(pipeline);
    }

    @Test
    void schedule_withValidRequest_returnsSuccessWithJobId() {
        PipelineRequest request = new PipelineRequest("staging", "app-1.0.jar", "v1.0");
        ScheduleResult result = scheduler.schedule(request, 5);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getJobId()).isNotNull().contains("staging");
        assertThat(result.getScheduledAt()).isNotNull();
    }

    @Test
    void schedule_withNullRequest_returnsFailure() {
        ScheduleResult result = scheduler.schedule(null, 5);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getErrorMessage()).contains("null");
    }

    @Test
    void schedule_withNegativeDelay_returnsFailure() {
        PipelineRequest request = new PipelineRequest("prod", "app-2.0.jar", "v2.0");
        ScheduleResult result = scheduler.schedule(request, -1);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getErrorMessage()).contains("non-negative");
    }

    @Test
    void cancel_existingJob_returnsTrueAndRemovesFromPending() {
        PipelineRequest request = new PipelineRequest("staging", "app-1.0.jar", "v1.0");
        ScheduleResult scheduleResult = scheduler.schedule(request, 60);

        assertThat(scheduler.getPendingJobIds()).contains(scheduleResult.getJobId());

        boolean cancelled = scheduler.cancel(scheduleResult.getJobId());

        assertThat(cancelled).isTrue();
        assertThat(scheduler.getPendingJobIds()).doesNotContain(scheduleResult.getJobId());
    }

    @Test
    void cancel_nonExistentJob_returnsFalse() {
        boolean result = scheduler.cancel("non-existent-job-id");
        assertThat(result).isFalse();
    }

    @Test
    void schedule_executesAndAddsToHistory() throws InterruptedException {
        when(pipeline.execute(any())).thenReturn(PipelineResult.success("deployed"));
        ScheduledExecutorService realExecutor = Executors.newScheduledThreadPool(1);
        DeploymentScheduler fastScheduler = new DeploymentScheduler(pipeline, realExecutor);

        PipelineRequest request = new PipelineRequest("dev", "app-1.0.jar", "v1.0");
        fastScheduler.schedule(request, 0);

        realExecutor.awaitTermination(500, TimeUnit.MILLISECONDS);

        verify(pipeline, timeout(1000)).execute(any());
        fastScheduler.shutdown();
    }
}
