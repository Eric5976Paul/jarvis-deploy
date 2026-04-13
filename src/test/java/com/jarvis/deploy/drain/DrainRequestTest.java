package com.jarvis.deploy.drain;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

class DrainRequestTest {

    @Test
    void constructor_setsAllFields() {
        DrainRequest req = new DrainRequest("prod", "api-service", Duration.ofSeconds(30), true);

        assertThat(req.getEnvironment()).isEqualTo("prod");
        assertThat(req.getAppName()).isEqualTo("api-service");
        assertThat(req.getTimeout()).isEqualTo(Duration.ofSeconds(30));
        assertThat(req.isForceOnTimeout()).isTrue();
    }

    @Test
    void constructor_throwsOnNullEnvironment() {
        assertThatNullPointerException().isThrownBy(
                () -> new DrainRequest(null, "app", Duration.ofSeconds(10), false)
        ).withMessageContaining("environment");
    }

    @Test
    void constructor_throwsOnNullAppName() {
        assertThatNullPointerException().isThrownBy(
                () -> new DrainRequest("staging", null, Duration.ofSeconds(10), false)
        ).withMessageContaining("appName");
    }

    @Test
    void constructor_throwsOnNullTimeout() {
        assertThatNullPointerException().isThrownBy(
                () -> new DrainRequest("staging", "app", null, false)
        ).withMessageContaining("timeout");
    }

    @Test
    void toString_containsKeyFields() {
        DrainRequest req = new DrainRequest("dev", "worker", Duration.ofSeconds(15), false);
        String str = req.toString();

        assertThat(str).contains("dev");
        assertThat(str).contains("worker");
    }
}
