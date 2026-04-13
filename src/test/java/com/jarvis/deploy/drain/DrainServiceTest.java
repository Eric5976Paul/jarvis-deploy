package com.jarvis.deploy.drain;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class DrainServiceTest {

    private static final String ENV = "staging";
    private static final String APP = "my-service";

    @Test
    void drain_completesImmediately_whenAlreadyIdle() {
        DrainService service = new DrainService(() -> true);
        DrainRequest request = new DrainRequest(ENV, APP, Duration.ofSeconds(5), false);

        DrainResult result = service.drain(request);

        assertThat(result.isSuccessful()).isTrue();
        assertThat(result.getStatus()).isEqualTo(DrainResult.Status.DRAINED);
        assertThat(result.getEnvironment()).isEqualTo(ENV);
        assertThat(result.getAppName()).isEqualTo(APP);
    }

    @Test
    void drain_completesAfterSeveralPolls() {
        AtomicInteger callCount = new AtomicInteger(0);
        // Returns idle only on 3rd call
        DrainService service = new DrainService(() -> callCount.incrementAndGet() >= 3);
        DrainRequest request = new DrainRequest(ENV, APP, Duration.ofSeconds(10), false);

        DrainResult result = service.drain(request);

        assertThat(result.isSuccessful()).isTrue();
        assertThat(result.getStatus()).isEqualTo(DrainResult.Status.DRAINED);
        assertThat(callCount.get()).isGreaterThanOrEqualTo(3);
    }

    @Test
    void drain_timesOut_whenNeverIdle_andForceIsFalse() {
        DrainService service = new DrainService(() -> false);
        DrainRequest request = new DrainRequest(ENV, APP, Duration.ofMillis(600), false);

        DrainResult result = service.drain(request);

        assertThat(result.isSuccessful()).isFalse();
        assertThat(result.getStatus()).isEqualTo(DrainResult.Status.TIMED_OUT);
        assertThat(result.getMessage()).contains("timed out");
    }

    @Test
    void drain_forcesStop_whenNeverIdle_andForceIsTrue() {
        DrainService service = new DrainService(() -> false);
        DrainRequest request = new DrainRequest(ENV, APP, Duration.ofMillis(600), true);

        DrainResult result = service.drain(request);

        assertThat(result.isSuccessful()).isTrue();
        assertThat(result.getStatus()).isEqualTo(DrainResult.Status.FORCED);
        assertThat(result.getMessage()).contains("force-stopped");
    }

    @Test
    void drain_fails_whenProbeThrowsException() {
        DrainService service = new DrainService(() -> {
            throw new RuntimeException("connection refused");
        });
        DrainRequest request = new DrainRequest(ENV, APP, Duration.ofSeconds(5), false);

        DrainResult result = service.drain(request);

        assertThat(result.isSuccessful()).isFalse();
        assertThat(result.getStatus()).isEqualTo(DrainResult.Status.FAILED);
        assertThat(result.getMessage()).contains("connection refused");
    }

    @Test
    void drainResult_completedAt_isSet() {
        DrainService service = new DrainService(() -> true);
        DrainRequest request = new DrainRequest(ENV, APP, Duration.ofSeconds(5), false);

        DrainResult result = service.drain(request);

        assertThat(result.getCompletedAt()).isNotNull();
    }
}
