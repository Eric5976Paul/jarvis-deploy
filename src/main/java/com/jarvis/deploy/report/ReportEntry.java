package com.jarvis.deploy.report;

import java.time.Instant;
import java.util.Objects;

public class ReportEntry {

    private final String deploymentId;
    private final String environment;
    private final String version;
    private final String status;
    private final Instant timestamp;

    public ReportEntry(String deploymentId, String environment, String version, String status, Instant timestamp) {
        this.deploymentId = Objects.requireNonNull(deploymentId);
        this.environment  = Objects.requireNonNull(environment);
        this.version      = Objects.requireNonNull(version);
        this.status       = Objects.requireNonNull(status);
        this.timestamp    = Objects.requireNonNull(timestamp);
    }

    public String deploymentId() { return deploymentId; }
    public String environment()  { return environment; }
    public String version()      { return version; }
    public String status()       { return status; }
    public Instant timestamp()   { return timestamp; }

    public static ReportEntry of(String id, String env, String version, String status) {
        return new ReportEntry(id, env, version, status, Instant.now());
    }

    @Override
    public String toString() {
        return String.format("ReportEntry{id=%s, env=%s, version=%s, status=%s}",
            deploymentId, environment, version, status);
    }
}
