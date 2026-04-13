package com.jarvis.deploy.artifact;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class ArtifactResolutionResultTest {

    @TempDir
    Path tempDir;

    @Test
    void success_hasCorrectState() {
        Path path = tempDir.resolve("app-1.0.jar");
        ArtifactResolutionResult result = ArtifactResolutionResult.success(path);

        assertTrue(result.isSuccess());
        assertFalse(result.isFailure());
        assertTrue(result.getArtifactPath().isPresent());
        assertEquals(path, result.getArtifactPath().get());
        assertTrue(result.getErrorMessage().isEmpty());
    }

    @Test
    void failure_hasCorrectState() {
        ArtifactResolutionResult result = ArtifactResolutionResult.failure("File not found");

        assertFalse(result.isSuccess());
        assertTrue(result.isFailure());
        assertTrue(result.getArtifactPath().isEmpty());
        assertTrue(result.getErrorMessage().isPresent());
        assertEquals("File not found", result.getErrorMessage().get());
    }

    @Test
    void getFileSizeBytes_forExistingFile_returnsSize() throws IOException {
        File jar = tempDir.resolve("app-2.0.jar").toFile();
        assertTrue(jar.createNewFile());
        // write some bytes
        java.nio.file.Files.writeString(jar.toPath(), "dummy content");

        ArtifactResolutionResult result = ArtifactResolutionResult.success(jar.toPath());
        assertTrue(result.getFileSizeBytes() > 0);
    }

    @Test
    void getFileSizeBytes_forFailureResult_returnsZero() {
        ArtifactResolutionResult result = ArtifactResolutionResult.failure("error");
        assertEquals(0L, result.getFileSizeBytes());
    }

    @Test
    void toString_successIncludesPath() {
        Path path = tempDir.resolve("app.jar");
        ArtifactResolutionResult result = ArtifactResolutionResult.success(path);
        assertTrue(result.toString().contains("success=true"));
    }

    @Test
    void toString_failureIncludesError() {
        ArtifactResolutionResult result = ArtifactResolutionResult.failure("missing");
        assertTrue(result.toString().contains("missing"));
        assertTrue(result.toString().contains("success=false"));
    }
}
