package com.jarvis.deploy.stealth;

import java.util.Optional;

/**
 * Encapsulates the outcome of a stealth mode activation or deactivation.
 */
public class StealthModeResult {

    public enum Status { ACTIVATED, DEACTIVATED, ALREADY_ACTIVE, NOT_FOUND, REJECTED }

    private final Status status;
    private final String message;
    private final StealthMode stealthMode;

    private StealthModeResult(Status status, String message, StealthMode stealthMode) {
        this.status = status;
        this.message = message;
        this.stealthMode = stealthMode;
    }

    public static StealthModeResult activated(StealthMode mode) {
        return new StealthModeResult(Status.ACTIVATED, "Stealth mode activated.", mode);
    }

    public static StealthModeResult deactivated(String deploymentId) {
        return new StealthModeResult(Status.DEACTIVATED,
                "Stealth mode deactivated for: " + deploymentId, null);
    }

    public static StealthModeResult alreadyActive(StealthMode mode) {
        return new StealthModeResult(Status.ALREADY_ACTIVE,
                "Stealth mode is already active.", mode);
    }

    public static StealthModeResult notFound(String deploymentId) {
        return new StealthModeResult(Status.NOT_FOUND,
                "No active stealth mode for: " + deploymentId, null);
    }

    public static StealthModeResult rejected(String reason) {
        return new StealthModeResult(Status.REJECTED, reason, null);
    }

    public Status getStatus() { return status; }
    public String getMessage() { return message; }
    public Optional<StealthMode> getStealthMode() { return Optional.ofNullable(stealthMode); }
    public boolean isSuccess() {
        return status == Status.ACTIVATED || status == Status.DEACTIVATED;
    }

    @Override
    public String toString() {
        return "StealthModeResult{status=" + status + ", message='" + message + "'}";
    }
}
