package com.jarvis.deploy.secret;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Provides secret resolution for deployment configurations.
 * Supports environment variable and inline value sources.
 */
public class SecretProvider {

    public enum SourceType {
        ENV, INLINE, VAULT
    }

    private final Map<String, String> inlineSecrets = new HashMap<>();
    private final Map<String, String> envOverrides = new HashMap<>();

    public SecretProvider() {}

    /**
     * Register an inline secret (e.g. from jarvis.yml).
     */
    public void registerInlineSecret(String key, String value) {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("Secret key must not be blank");
        }
        inlineSecrets.put(key, value);
    }

    /**
     * Override an env variable value (useful for testing).
     */
    public void registerEnvOverride(String key, String value) {
        envOverrides.put(key, value);
    }

    /**
     * Resolve a secret by key, checking inline secrets first, then env variables.
     */
    public Optional<String> resolve(String key) {
        if (key == null || key.isBlank()) {
            return Optional.empty();
        }
        if (inlineSecrets.containsKey(key)) {
            return Optional.of(inlineSecrets.get(key));
        }
        String envValue = envOverrides.getOrDefault(key, System.getenv(key));
        return Optional.ofNullable(envValue);
    }

    /**
     * Resolve a secret or throw if not found.
     */
    public String resolveOrThrow(String key) {
        return resolve(key).orElseThrow(() ->
            new SecretResolutionException("Secret not found for key: " + key));
    }

    /**
     * Determine the source type for a given key.
     */
    public SourceType sourceTypeFor(String key) {
        if (inlineSecrets.containsKey(key)) return SourceType.INLINE;
        String envValue = envOverrides.getOrDefault(key, System.getenv(key));
        if (envValue != null) return SourceType.ENV;
        return SourceType.VAULT;
    }

    public int registeredInlineCount() {
        return inlineSecrets.size();
    }
}
