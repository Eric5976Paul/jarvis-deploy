package com.jarvis.deploy.promotion;

import com.jarvis.deploy.deployment.DeploymentHistory;
import com.jarvis.deploy.deployment.DeploymentRecord;
import com.jarvis.deploy.env.EnvironmentValidator;
import com.jarvis.deploy.env.ValidationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PromotionServiceTest {

    @Mock private EnvironmentValidator environmentValidator;
    @Mock private DeploymentHistory deploymentHistory;
    @Mock private DeploymentRecord deploymentRecord;

    private PromotionService promotionService;

    @BeforeEach
    void setUp() {
        promotionService = new PromotionService(environmentValidator, deploymentHistory);
    }

    @Test
    void promote_success_whenAllConditionsMet() {
        PromotionRequest request = new PromotionRequest("my-app", "1.2.0", "staging", "production", "alice");
        when(environmentValidator.validate("staging")).thenReturn(ValidationResult.valid());
        when(environmentValidator.validate("production")).thenReturn(ValidationResult.valid());
        when(deploymentRecord.getVersion()).thenReturn("1.2.0");
        when(deploymentHistory.getLatestRecord("my-app", "staging")).thenReturn(Optional.of(deploymentRecord));

        PromotionResult result = promotionService.promote(request);

        assertThat(result.isSuccessful()).isTrue();
        assertThat(result.getStatus()).isEqualTo(PromotionResult.Status.SUCCESS);
        assertThat(result.getMessage()).contains("my-app").contains("1.2.0").contains("production");
        verify(deploymentHistory).record(any(DeploymentRecord.class));
    }

    @Test
    void promote_fails_whenSourceEnvironmentInvalid() {
        PromotionRequest request = new PromotionRequest("my-app", "1.2.0", "staging", "production", "alice");
        when(environmentValidator.validate("staging")).thenReturn(ValidationResult.invalid(java.util.List.of("missing config")));

        PromotionResult result = promotionService.promote(request);

        assertThat(result.isSuccessful()).isFalse();
        assertThat(result.getStatus()).isEqualTo(PromotionResult.Status.FAILED);
        assertThat(result.getMessage()).contains("Source environment validation failed");
        verifyNoInteractions(deploymentHistory);
    }

    @Test
    void promote_skipped_whenNoRecordInSource() {
        PromotionRequest request = new PromotionRequest("my-app", "1.2.0", "staging", "production", "alice");
        when(environmentValidator.validate(anyString())).thenReturn(ValidationResult.valid());
        when(deploymentHistory.getLatestRecord("my-app", "staging")).thenReturn(Optional.empty());

        PromotionResult result = promotionService.promote(request);

        assertThat(result.getStatus()).isEqualTo(PromotionResult.Status.SKIPPED);
        assertThat(result.getMessage()).contains("No deployment record found");
    }

    @Test
    void promote_fails_onVersionMismatch() {
        PromotionRequest request = new PromotionRequest("my-app", "1.2.0", "staging", "production", "alice");
        when(environmentValidator.validate(anyString())).thenReturn(ValidationResult.valid());
        when(deploymentRecord.getVersion()).thenReturn("1.1.0");
        when(deploymentHistory.getLatestRecord("my-app", "staging")).thenReturn(Optional.of(deploymentRecord));

        PromotionResult result = promotionService.promote(request);

        assertThat(result.isSuccessful()).isFalse();
        assertThat(result.getMessage()).contains("Version mismatch").contains("1.2.0").contains("1.1.0");
    }

    @Test
    void promotionRequest_throwsWhenSameEnvironment() {
        assertThatThrownBy(() ->
                new PromotionRequest("my-app", "1.0.0", "prod", "prod", "bob"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("must differ");
    }
}
