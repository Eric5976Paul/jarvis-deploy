package com.jarvis.deploy.cli;

/**
 * Represents the result of a CLI command execution.
 */
public class CliResult {

    private final boolean success;
    private final String message;
    private final int exitCode;

    private CliResult(boolean success, String message, int exitCode) {
        this.success = success;
        this.message = message;
        this.exitCode = exitCode;
    }

    public static CliResult success(String message) {
        return new CliResult(true, message, 0);
    }

    public static CliResult failure(String message) {
        return new CliResult(false, message, 1);
    }

    public static CliResult failure(String message, int exitCode) {
        return new CliResult(false, message, exitCode);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public int getExitCode() {
        return exitCode;
    }

    public void print() {
        if (success) {
            System.out.println("[OK] " + message);
        } else {
            System.err.println("[ERROR] " + message);
        }
    }

    @Override
    public String toString() {
        return "CliResult{success=" + success + ", message='" + message + "', exitCode=" + exitCode + "}";
    }
}
