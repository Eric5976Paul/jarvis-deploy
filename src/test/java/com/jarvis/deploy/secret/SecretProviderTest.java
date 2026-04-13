package com.jarvis.deploy.secret;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class SecretProviderTest {

    private SecretProvider secretProvider;

    @BeforeEach
    void setUp() {
        secretProvider = new SecretProvider();
    }

    @Test
    void resolveInlineSecret_returnsValue() {
        secretProvider.registerInlineSecret("db.password", "s3cr3t");
        Optional<String> result = secretProvider.resolve("db.password");
        assertTrue(result.isPresent());
        assertEquals("s3cr3t", result.get());
    }

    @Test
    void resolveEnvOverride_returnsValue() {
        secretProvider.registerEnvOverride("API_KEY", "abc123");
        Optional<String> result = secretProvider.resolve("API_KEY");
        assertTrue(result.isPresent());
        assertEquals("abc123", result.get());
    }

    @Test
    void resolveUnknownKey_returnsEmpty() {
        Optional<String> result = secretProvider.resolve("nonexistent.key");
        assertFalse(result.isPresent());
    }

    @Test
    void resolveNullKey_returnsEmpty() {
        Optional<String> result = secretProvider.resolve(null);
        assertFalse(result.isPresent());
    }

    @Test
    void resolveOrThrow_missingKey_throwsException() {
        SecretResolutionException ex = assertThrows(SecretResolutionException.class,
            () -> secretProvider.resolveOrThrow("missing.key"));
        assertTrue(ex.getMessage().contains("missing.key"));
    }

    @Test
    void resolveOrThrow_presentKey_returnsValue() {
        secretProvider.registerInlineSecret("token", "mytoken");
        assertEquals("mytoken", secretProvider.resolveOrThrow("token"));
    }

    @Test
    void registerInlineSecret_blankKey_throwsException() {
        assertThrows(IllegalArgumentException.class,
            () -> secretProvider.registerInlineSecret(" ", "value"));
    }

    @Test
    void sourceTypeFor_inlineKey_returnsInline() {
        secretProvider.registerInlineSecret("inline.key", "val");
        assertEquals(SecretProvider.SourceType.INLINE, secretProvider.sourceTypeFor("inline.key"));
    }

    @Test
    void sourceTypeFor_envKey_returnsEnv() {
        secretProvider.registerEnvOverride("ENV_KEY", "envval");
        assertEquals(SecretProvider.SourceType.ENV, secretProvider.sourceTypeFor("ENV_KEY"));
    }

    @Test
    void sourceTypeFor_unknownKey_returnsVault() {
        assertEquals(SecretProvider.SourceType.VAULT, secretProvider.sourceTypeFor("unknown.key"));
    }

    @Test
    void registeredInlineCount_reflectsRegistrations() {
        assertEquals(0, secretProvider.registeredInlineCount());
        secretProvider.registerInlineSecret("k1", "v1");
        secretProvider.registerInlineSecret("k2", "v2");
        assertEquals(2, secretProvider.registeredInlineCount());
    }

    @Test
    void inlineSecretTakesPrecedenceOverEnv() {
        secretProvider.registerInlineSecret("KEY", "inline-value");
        secretProvider.registerEnvOverride("KEY", "env-value");
        assertEquals("inline-value", secretProvider.resolveOrThrow("KEY"));
    }
}
