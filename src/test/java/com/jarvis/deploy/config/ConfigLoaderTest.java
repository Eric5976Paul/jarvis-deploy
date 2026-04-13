package com.jarvis.deploy.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class ConfigLoaderTest {

    private final ConfigLoader loader = new ConfigLoader();

    @Test
    void loadsValidConfigSuccessfully(@TempDir Path tempDir) throws IOException {
        Path config = tempDir.resolve("jarvis.yml");
        Files.writeString(config,
                "appName: my-service\n" +
                "environment: staging\n" +
                "artifactPath: build/my-service.jar\n" +
                "deployDir: /opt/apps/my-service\n" +
                "maxRollbackVersions: 3\n");

        DeploymentConfig result = loader.load(config);

        assertEquals("my-service", result.getAppName());
        assertEquals("staging", result.getEnvironment());
        assertEquals("build/my-service.jar", result.getArtifactPath());
        assertEquals("/opt/apps/my-service", result.getDeployDir());
        assertEquals(3, result.getMaxRollbackVersions());
    }

    @Test
    void appliesDefaultEnvironmentWhenMissing(@TempDir Path tempDir) throws IOException {
        Path config = tempDir.resolve("jarvis.yml");
        Files.writeString(config,
                "appName: my-service\n" +
                "artifactPath: build/my-service.jar\n");

        DeploymentConfig result = loader.load(config);

        assertEquals("default", result.getEnvironment());
        assertTrue(result.getDeployDir().contains("my-service"));
    }

    @Test
    void throwsWhenConfigFileNotFound() {
        Path missing = Path.of("/non/existent/jarvis.yml");
        assertThrows(IOException.class, () -> loader.load(missing));
    }

    @Test
    void throwsWhenAppNameIsMissing(@TempDir Path tempDir) throws IOException {
        Path config = tempDir.resolve("jarvis.yml");
        Files.writeString(config, "artifactPath: build/my-service.jar\n");

        assertThrows(IllegalArgumentException.class, () -> loader.load(config));
    }

    @Test
    void throwsWhenArtifactPathIsMissing(@TempDir Path tempDir) throws IOException {
        Path config = tempDir.resolve("jarvis.yml");
        Files.writeString(config, "appName: my-service\n");

        assertThrows(IllegalArgumentException.class, () -> loader.load(config));
    }

    @Test
    void appliesDefaultMaxRollbackVersionsWhenMissing(@TempDir Path tempDir) throws IOException {
        Path config = tempDir.resolve("jarvis.yml");
        Files.writeString(config,
                "appName: my-service\n" +
                "artifactPath: build/my-service.jar\n");

        DeploymentConfig result = loader.load(config);

        // Verify that a sensible default is applied when maxRollbackVersions is not specified
        assertTrue(result.getMaxRollbackVersions() > 0,
                "Expected a positive default for maxRollbackVersions, got: " + result.getMaxRollbackVersions());
    }
}
