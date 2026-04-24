package com.jarvis.deploy.fingerprint;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Objects;

/**
 * Represents a deterministic fingerprint for a deployment, computed from
 * environment, artifact, and version information. Used to detect duplicate
 * or redundant deployments before execution.
 */
public class DeploymentFingerprint {

    private final String environment;
    private final String artifactId;
    private final String version;
    private final String hash;
    private final Instant computedAt;

    private DeploymentFingerprint(String environment, String artifactId, String version) {
        this.environment = Objects.requireNonNull(environment, "environment must not be null");
        this.artifactId = Objects.requireNonNull(artifactId, "artifactId must not be null");
        this.version = Objects.requireNonNull(version, "version must not be null");
        this.computedAt = Instant.now();
        this.hash = computeHash(environment, artifactId, version);
    }

    public static DeploymentFingerprint of(String environment, String artifactId, String version) {
        return new DeploymentFingerprint(environment, artifactId, version);
    }

    private static String computeHash(String environment, String artifactId, String version) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String input = environment + ":" + artifactId + ":" + version;
            byte[] bytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(bytes).substring(0, 16);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }

    public boolean matches(DeploymentFingerprint other) {
        return other != null && this.hash.equals(other.hash);
    }

    public String getEnvironment() { return environment; }
    public String getArtifactId() { return artifactId; }
    public String getVersion() { return version; }
    public String getHash() { return hash; }
    public Instant getComputedAt() { return computedAt; }

    @Override
    public String toString() {
        return String.format("DeploymentFingerprint{env='%s', artifact='%s', version='%s', hash='%s'}",
                environment, artifactId, version, hash);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DeploymentFingerprint)) return false;
        DeploymentFingerprint that = (DeploymentFingerprint) o;
        return hash.equals(that.hash);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hash);
    }
}
