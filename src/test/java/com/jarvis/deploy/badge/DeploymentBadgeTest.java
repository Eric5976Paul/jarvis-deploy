package com.jarvis.deploy.badge;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class DeploymentBadgeTest {

    private static final Instant NOW = Instant.parse("2024-06-01T12:00:00Z");

    @Test
    void shouldCreateBadgeWithCorrectFields() {
        DeploymentBadge badge = new DeploymentBadge("production", "1.4.2", BadgeStatus.SUCCESS, NOW);
        assertEquals("production", badge.getEnvironment());
        assertEquals("1.4.2", badge.getVersion());
        assertEquals(BadgeStatus.SUCCESS, badge.getStatus());
        assertEquals(NOW, badge.getGeneratedAt());
    }

    @Test
    void shouldResolveColorForSuccess() {
        DeploymentBadge badge = new DeploymentBadge("staging", "2.0.0", BadgeStatus.SUCCESS, NOW);
        assertEquals("brightgreen", badge.getColor());
    }

    @Test
    void shouldResolveColorForFailed() {
        DeploymentBadge badge = new DeploymentBadge("staging", "2.0.0", BadgeStatus.FAILED, NOW);
        assertEquals("red", badge.getColor());
    }

    @Test
    void shouldResolveColorForInProgress() {
        DeploymentBadge badge = new DeploymentBadge("dev", "1.0.0", BadgeStatus.IN_PROGRESS, NOW);
        assertEquals("yellow", badge.getColor());
    }

    @Test
    void shouldResolveColorForRolledBack() {
        DeploymentBadge badge = new DeploymentBadge("production", "1.3.0", BadgeStatus.ROLLED_BACK, NOW);
        assertEquals("orange", badge.getColor());
    }

    @Test
    void shouldResolveColorForPending() {
        DeploymentBadge badge = new DeploymentBadge("qa", "0.9.0", BadgeStatus.PENDING, NOW);
        assertEquals("lightgrey", badge.getColor());
    }

    @Test
    void shouldGenerateShieldsUrl() {
        DeploymentBadge badge = new DeploymentBadge("production", "1.4.2", BadgeStatus.SUCCESS, NOW);
        String url = badge.toShieldsUrl();
        assertTrue(url.startsWith("https://img.shields.io/badge/"), "URL should start with shields.io base");
        assertTrue(url.contains("brightgreen"), "URL should contain the color");
        assertTrue(url.contains("production"), "URL should contain environment");
        assertTrue(url.contains("1.4.2"), "URL should contain version");
    }

    @Test
    void shouldEscapeHyphensInShieldsUrl() {
        DeploymentBadge badge = new DeploymentBadge("pre-prod", "2.0.0-RC1", BadgeStatus.IN_PROGRESS, NOW);
        String url = badge.toShieldsUrl();
        assertTrue(url.contains("pre--prod"), "Hyphens in env should be escaped");
        assertTrue(url.contains("2.0.0--RC1"), "Hyphens in version should be escaped");
    }

    @Test
    void shouldThrowOnNullEnvironment() {
        assertThrows(NullPointerException.class,
                () -> new DeploymentBadge(null, "1.0.0", BadgeStatus.SUCCESS, NOW));
    }

    @Test
    void shouldThrowOnNullVersion() {
        assertThrows(NullPointerException.class,
                () -> new DeploymentBadge("prod", null, BadgeStatus.SUCCESS, NOW));
    }

    @Test
    void shouldImplementEquality() {
        DeploymentBadge b1 = new DeploymentBadge("prod", "1.0.0", BadgeStatus.SUCCESS, NOW);
        DeploymentBadge b2 = new DeploymentBadge("prod", "1.0.0", BadgeStatus.SUCCESS, Instant.now());
        assertEquals(b1, b2);
        assertEquals(b1.hashCode(), b2.hashCode());
    }

    @Test
    void shouldNotBeEqualWithDifferentStatus() {
        DeploymentBadge b1 = new DeploymentBadge("prod", "1.0.0", BadgeStatus.SUCCESS, NOW);
        DeploymentBadge b2 = new DeploymentBadge("prod", "1.0.0", BadgeStatus.FAILED, NOW);
        assertNotEquals(b1, b2);
    }
}
