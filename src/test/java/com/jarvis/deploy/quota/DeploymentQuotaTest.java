package com.jarvis.deploy.quota;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.*;

class DeploymentQuotaTest {

    private DeploymentQuota quota;

    @BeforeEach
    void setUp() {
        quota = new DeploymentQuota(3, Duration.ofMinutes(10));
    }

    @Test
    void check_allowsWhenUnderQuota() {
        QuotaCheckResult result = quota.check("staging");
        assertThat(result.isAllowed()).isTrue();
        assertThat(result.getCurrentCount()).isEqualTo(0);
        assertThat(result.getMaxAllowed()).isEqualTo(3);
        assertThat(result.getRemainingQuota()).isEqualTo(3);
        assertThat(result.getEnvironment()).isEqualTo("staging");
    }

    @Test
    void recordAndCheck_incrementsCountAndAllows() {
        QuotaCheckResult r1 = quota.recordAndCheck("prod");
        QuotaCheckResult r2 = quota.recordAndCheck("prod");

        assertThat(r1.isAllowed()).isTrue();
        assertThat(r1.getCurrentCount()).isEqualTo(1);
        assertThat(r2.isAllowed()).isTrue();
        assertThat(r2.getCurrentCount()).isEqualTo(2);
    }

    @Test
    void recordAndCheck_deniesWhenQuotaExceeded() {
        quota.recordAndCheck("prod");
        quota.recordAndCheck("prod");
        quota.recordAndCheck("prod");

        QuotaCheckResult result = quota.recordAndCheck("prod");

        assertThat(result.isAllowed()).isFalse();
        assertThat(result.getCurrentCount()).isEqualTo(3);
        assertThat(result.getRemainingQuota()).isEqualTo(0);
        assertThat(result.getReason()).isPresent();
        assertThat(result.getReason().get()).contains("Quota exceeded");
    }

    @Test
    void reset_clearsCounterForEnvironment() {
        quota.recordAndCheck("dev");
        quota.recordAndCheck("dev");
        quota.reset("dev");

        QuotaCheckResult result = quota.check("dev");
        assertThat(result.getCurrentCount()).isEqualTo(0);
        assertThat(result.isAllowed()).isTrue();
    }

    @Test
    void quotas_areIsolatedPerEnvironment() {
        quota.recordAndCheck("prod");
        quota.recordAndCheck("prod");
        quota.recordAndCheck("prod");

        QuotaCheckResult stagingResult = quota.check("staging");
        assertThat(stagingResult.isAllowed()).isTrue();
        assertThat(stagingResult.getCurrentCount()).isEqualTo(0);
    }

    @Test
    void constructor_throwsOnInvalidMaxDeployments() {
        assertThatThrownBy(() -> new DeploymentQuota(0, Duration.ofMinutes(5)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("maxDeploymentsPerWindow must be positive");
    }

    @Test
    void constructor_throwsOnNullOrZeroDuration() {
        assertThatThrownBy(() -> new DeploymentQuota(5, Duration.ZERO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("windowDuration must be a positive duration");

        assertThatThrownBy(() -> new DeploymentQuota(5, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void check_includesResetAtWhenDeploymentsExist() {
        quota.recordAndCheck("staging");
        QuotaCheckResult result = quota.check("staging");
        assertThat(result.getResetAt()).isPresent();
    }

    @Test
    void check_noResetAtWhenNoDeployments() {
        QuotaCheckResult result = quota.check("staging");
        assertThat(result.getResetAt()).isEmpty();
    }
}
