package com.jarvis.deploy.artifact;

import com.jarvis.deploy.config.DeploymentConfig;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Resolves and validates deployment artifacts (JAR files) for a given environment.
 */
public class ArtifactResolver {

    private static final Logger log = Logger.getLogger(ArtifactResolver.class.getName());
    private static final String JAR_EXTENSION = ".jar";

    private final DeploymentConfig config;

    public ArtifactResolver(DeploymentConfig config) {
        this.config = config;
    }

    /**
     * Resolves the artifact path for the given version.
     *
     * @param version the application version to resolve
     * @return an ArtifactResolutionResult indicating success or failure
     */
    public ArtifactResolutionResult resolve(String version) {
        if (version == null || version.isBlank()) {
            return ArtifactResolutionResult.failure("Version must not be null or blank");
        }

        String artifactDir = config.getArtifactDirectory();
        if (artifactDir == null || artifactDir.isBlank()) {
            return ArtifactResolutionResult.failure("Artifact directory is not configured");
        }

        String fileName = config.getAppName() + "-" + version + JAR_EXTENSION;
        Path artifactPath = Paths.get(artifactDir, fileName);
        File artifactFile = artifactPath.toFile();

        log.info("Resolving artifact: " + artifactPath);

        if (!artifactFile.exists()) {
            return ArtifactResolutionResult.failure(
                "Artifact not found: " + artifactPath.toAbsolutePath());
        }

        if (!artifactFile.canRead()) {
            return ArtifactResolutionResult.failure(
                "Artifact is not readable: " + artifactPath.toAbsolutePath());
        }

        log.info("Artifact resolved successfully: " + artifactPath);
        return ArtifactResolutionResult.success(artifactPath);
    }

    /**
     * Returns the expected artifact path without existence checks.
     */
    public Optional<Path> expectedPath(String version) {
        if (version == null || version.isBlank()) return Optional.empty();
        String artifactDir = config.getArtifactDirectory();
        if (artifactDir == null || artifactDir.isBlank()) return Optional.empty();
        String fileName = config.getAppName() + "-" + version + JAR_EXTENSION;
        return Optional.of(Paths.get(artifactDir, fileName));
    }
}
