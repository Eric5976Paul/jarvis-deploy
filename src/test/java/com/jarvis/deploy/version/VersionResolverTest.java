package com.jarvis.deploy.version;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class VersionResolverTest {

    private VersionResolver resolver;

    @BeforeEach
    void setUp() {
        resolver = new VersionResolver();
    }

    @Test
    void resolveValidSemver() {
        VersionResolutionResult result = resolver.resolve("1.2.3");
        assertTrue(result.isSuccess());
        assertEquals("1.2.3", result.getVersion());
        assertEquals(VersionType.SEMVER, result.getType());
    }

    @Test
    void resolveSnapshot() {
        VersionResolutionResult result = resolver.resolve("2.0.0-SNAPSHOT");
        assertTrue(result.isSuccess());
        assertEquals(VersionType.SNAPSHOT, result.getType());
    }

    @Test
    void resolveTimestamp() {
        VersionResolutionResult result = resolver.resolve("20240315.143000");
        assertTrue(result.isSuccess());
        assertEquals(VersionType.TIMESTAMP, result.getType());
    }

    @Test
    void resolveBlankReturnsFailed() {
        VersionResolutionResult result = resolver.resolve("  ");
        assertFalse(result.isSuccess());
        assertNotNull(result.getErrorMessage());
    }

    @Test
    void resolveNullReturnsFailed() {
        VersionResolutionResult result = resolver.resolve(null);
        assertFalse(result.isSuccess());
    }

    @Test
    void resolveUnknownFormatFails() {
        VersionResolutionResult result = resolver.resolve("latest");
        assertFalse(result.isSuccess());
        assertTrue(result.getErrorMessage().contains("Unrecognized"));
    }

    @Test
    void isStableReturnsTrueForSemver() {
        assertTrue(resolver.isStable("3.1.4"));
    }

    @Test
    void isStableReturnsFalseForSnapshot() {
        assertFalse(resolver.isStable("3.1.4-SNAPSHOT"));
    }

    @Test
    void normalizeStripsLeadingV() {
        Optional<String> result = resolver.normalize("v1.0.0");
        assertTrue(result.isPresent());
        assertEquals("1.0.0", result.get());
    }

    @Test
    void normalizeReturnsEmptyForUnknown() {
        Optional<String> result = resolver.normalize("latest");
        assertFalse(result.isPresent());
    }
}
