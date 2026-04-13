package com.jarvis.deploy.secret;

/**
 * Thrown when a required secret cannot be resolved from any available source.
 */
public class SecretResolutionException extends RuntimeException {

    private final String secretKey;

    public SecretResolutionException(String message) {
        super(message);
        this.secretKey = null;
    }

    public SecretResolutionException(String secretKey, String message) {
        super(message);
        this.secretKey = secretKey;
    }

    public SecretResolutionException(String secretKey, String message, Throwable cause) {
        super(message, cause);
        this.secretKey = secretKey;
    }

    public String getSecretKey() {
        return secretKey;
    }
}
