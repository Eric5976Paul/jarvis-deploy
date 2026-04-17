package com.jarvis.deploy.alerting;

import java.time.Instant;
import java.util.Objects;

public class DeploymentAlert {
    private final String id;
    private final String environment;
    private final AlertSeverity severity;
    private final String message;
    private final Instant triggeredAt;

    public DeploymentAlert(String id, String environment, AlertSeverity severity, String message) {
        this.id = Objects.requireNonNull(id);
        this.environment = Objects.requireNonNull(environment);
        this.severity = Objects.requireNonNull(severity);
        this.message = Objects.requireNonNull(message);
        this.triggeredAt = Instant.now();
    }

    public String getId() { return id; }
    public String getEnvironment() { return environment; }
    public AlertSeverity getSeverity() { return severity; }
    public String getMessage() { return message; }
    public Instant getTriggeredAt() { return triggeredAt; }

    @Override
    public String toString() {
        return String.format("[%s] %s (%s): %s", severity, environment, id, message);
    }
}
