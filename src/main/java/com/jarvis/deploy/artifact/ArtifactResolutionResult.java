package com.jarvis.deploy.artifact;

import java.nio.file.Path;
import java.util.Optional;

/**
 * Represents the outcome of an artifact resolution attempt.
 */
public class ArtifactResolutionResult {

    private final boolean success;
    private final Path artifactPath;
    private final String errorMessage;

    private ArtifactResolutionResult(boolean success, Path artifactPath, String errorMessage) {
        this.success = success;
        this.artifactPath = artifactPath;
        this.errorMessage = errorMessage;
    }

    public static ArtifactResolutionResult success(Path artifactPath) {
        return new ArtifactResolutionResult(true, artifactPath, null);
    }

    public static ArtifactResolutionResult failure(String errorMessage) {
        return new ArtifactResolutionResult(false, null, errorMessage);
    }

    public boolean isSuccess() {
        return success;
    }

    public boolean isFailure() {
        return !success;
    }

    public Optional<Path> getArtifactPath() {
        return Optional.ofNullable(artifactPath);
    }

    public Optional<String> getErrorMessage() {
        return Optional.ofNullable(errorMessage);
    }

    public long getFileSizeBytes() {
        if (artifactPath == null) return 0L;
        return artifactPath.toFile().length();
    }

    @Override
    public String toString() {
        if (success) {
            return "ArtifactResolutionResult{success=true, path=" + artifactPath + "}";
        }
        return "ArtifactResolutionResult{success=false, error='" + errorMessage + "'}";
    }
}
