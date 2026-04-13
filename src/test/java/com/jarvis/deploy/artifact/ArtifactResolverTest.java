package com.jarvis.deploy.artifact;

import com.jarvis.deploy.config.DeploymentConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ArtifactResolverTest {

    @TempDir
    Path tempDir;

    private DeploymentConfig config;
    private ArtifactResolver resolver;

    @BeforeEach
    void setUp() {
        config = mock(DeploymentConfig.class);
        when(config.getAppName()).thenReturn("my-app");
        when(config.getArtifactDirectory()).thenReturn(tempDir.toString());
        resolver = new ArtifactResolver(config);
    }

    @Test
    void resolve_withValidArtifact_returnsSuccess() throws IOException {
        File jar = tempDir.resolve("my-app-1.0.0.jar").toFile();
        assertTrue(jar.createNewFile());

        ArtifactResolutionResult result = resolver.resolve("1.0.0");

        assertTrue(result.isSuccess());
        assertTrue(result.getArtifactPath().isPresent());
        assertTrue(result.getArtifactPath().get().toString().endsWith("my-app-1.0.0.jar"));
    }

    @Test
    void resolve_withMissingArtifact_returnsFailure() {
        ArtifactResolutionResult result = resolver.resolve("9.9.9");

        assertTrue(result.isFailure());
        assertTrue(result.getErrorMessage().isPresent());
        assertTrue(result.getErrorMessage().get().contains("Artifact not found"));
    }

    @Test
    void resolve_withBlankVersion_returnsFailure() {
        ArtifactResolutionResult result = resolver.resolve("  ");

        assertTrue(result.isFailure());
        assertTrue(result.getErrorMessage().get().contains("Version must not be null or blank"));
    }

    @Test
    void resolve_withNullVersion_returnsFailure() {
        ArtifactResolutionResult result = resolver.resolve(null);

        assertTrue(result.isFailure());
    }

    @Test
    void resolve_withUnconfiguredArtifactDir_returnsFailure() {
        when(config.getArtifactDirectory()).thenReturn("");
        ArtifactResolutionResult result = resolver.resolve("1.0.0");

        assertTrue(result.isFailure());
        assertTrue(result.getErrorMessage().get().contains("Artifact directory is not configured"));
    }

    @Test
    void expectedPath_returnsCorrectPath() {
        var path = resolver.expectedPath("2.3.1");

        assertTrue(path.isPresent());
        assertTrue(path.get().toString().endsWith("my-app-2.3.1.jar"));
    }

    @Test
    void expectedPath_withNullVersion_returnsEmpty() {
        var path = resolver.expectedPath(null);
        assertTrue(path.isEmpty());
    }
}
