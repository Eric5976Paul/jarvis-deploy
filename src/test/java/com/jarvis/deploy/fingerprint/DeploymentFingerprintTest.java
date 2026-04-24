package com.jarvis.deploy.fingerprint;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DeploymentFingerprintTest {

    @Test
    void shouldComputeConsistentHashForSameInputs() {
        DeploymentFingerprint fp1 = DeploymentFingerprint.of("prod", "my-service", "1.2.3");
        DeploymentFingerprint fp2 = DeploymentFingerprint.of("prod", "my-service", "1.2.3");

        assertEquals(fp1.getHash(), fp2.getHash());
    }

    @Test
    void shouldProduceDifferentHashForDifferentEnvironment() {
        DeploymentFingerprint fp1 = DeploymentFingerprint.of("prod", "my-service", "1.2.3");
        DeploymentFingerprint fp2 = DeploymentFingerprint.of("staging", "my-service", "1.2.3");

        assertNotEquals(fp1.getHash(), fp2.getHash());
    }

    @Test
    void shouldProduceDifferentHashForDifferentVersion() {
        DeploymentFingerprint fp1 = DeploymentFingerprint.of("prod", "my-service", "1.2.3");
        DeploymentFingerprint fp2 = DeploymentFingerprint.of("prod", "my-service", "1.2.4");

        assertNotEquals(fp1.getHash(), fp2.getHash());
    }

    @Test
    void shouldProduceDifferentHashForDifferentArtifact() {
        DeploymentFingerprint fp1 = DeploymentFingerprint.of("prod", "service-a", "1.0.0");
        DeploymentFingerprint fp2 = DeploymentFingerprint.of("prod", "service-b", "1.0.0");

        assertNotEquals(fp1.getHash(), fp2.getHash());
    }

    @Test
    void matchesShouldReturnTrueForIdenticalFingerprints() {
        DeploymentFingerprint fp1 = DeploymentFingerprint.of("dev", "api", "2.0.0");
        DeploymentFingerprint fp2 = DeploymentFingerprint.of("dev", "api", "2.0.0");

        assertTrue(fp1.matches(fp2));
    }

    @Test
    void matchesShouldReturnFalseForDifferentFingerprints() {
        DeploymentFingerprint fp1 = DeploymentFingerprint.of("dev", "api", "2.0.0");
        DeploymentFingerprint fp2 = DeploymentFingerprint.of("prod", "api", "2.0.0");

        assertFalse(fp1.matches(fp2));
    }

    @Test
    void matchesShouldReturnFalseForNull() {
        DeploymentFingerprint fp = DeploymentFingerprint.of("prod", "svc", "1.0.0");
        assertFalse(fp.matches(null));
    }

    @Test
    void equalsShouldBeBasedOnHash() {
        DeploymentFingerprint fp1 = DeploymentFingerprint.of("prod", "svc", "1.0.0");
        DeploymentFingerprint fp2 = DeploymentFingerprint.of("prod", "svc", "1.0.0");

        assertEquals(fp1, fp2);
        assertEquals(fp1.hashCode(), fp2.hashCode());
    }

    @Test
    void shouldRejectNullEnvironment() {
        assertThrows(NullPointerException.class,
                () -> DeploymentFingerprint.of(null, "svc", "1.0.0"));
    }

    @Test
    void shouldRejectNullArtifactId() {
        assertThrows(NullPointerException.class,
                () -> DeploymentFingerprint.of("prod", null, "1.0.0"));
    }

    @Test
    void shouldRejectNullVersion() {
        assertThrows(NullPointerException.class,
                () -> DeploymentFingerprint.of("prod", "svc", null));
    }

    @Test
    void hashShouldBeSixteenCharacters() {
        DeploymentFingerprint fp = DeploymentFingerprint.of("prod", "svc", "1.0.0");
        assertEquals(16, fp.getHash().length());
    }

    @Test
    void toStringShouldContainAllFields() {
        DeploymentFingerprint fp = DeploymentFingerprint.of("prod", "my-svc", "3.1.0");
        String str = fp.toString();
        assertTrue(str.contains("prod"));
        assertTrue(str.contains("my-svc"));
        assertTrue(str.contains("3.1.0"));
        assertTrue(str.contains(fp.getHash()));
    }
}
