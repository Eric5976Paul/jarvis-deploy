package com.jarvis.deploy.audit;

import java.time.Instant;
import java.util.Objects;

/**
 * Represents a single auditable event in the deployment lifecycle.
 */
public class AuditEvent {

    public enum EventType {
        DEPLOY_STARTED,
        DEPLOY_SUCCEEDED,
        DEPLOY_FAILED,
        ROLLBACK_STARTED,
        ROLLBACK_SUCCEEDED,
        ROLLBACK_FAILED,
        HEALTH_CHECK_PASSED,
        HEALTH_CHECK_FAILED,
        LOCK_ACQUIRED,
        LOCK_RELEASED
    }

    private final EventType type;
    private final String environment;
    private final String version;
    private final String actor;
    private final String message;
    private final Instant timestamp;

    public AuditEvent(EventType type, String environment, String version, String actor, String message) {
        this.type = Objects.requireNonNull(type, "type must not be null");
        this.environment = Objects.requireNonNull(environment, "environment must not be null");
        this.version = version;
        this.actor = actor != null ? actor : "system";
        this.message = message != null ? message : "";
        this.timestamp = Instant.now();
    }

    public EventType getType() { return type; }
    public String getEnvironment() { return environment; }
    public String getVersion() { return version; }
    public String getActor() { return actor; }
    public String getMessage() { return message; }
    public Instant getTimestamp() { return timestamp; }

    @Override
    public String toString() {
        return String.format("[%s] %s | env=%s version=%s actor=%s | %s",
                timestamp, type, environment, version, actor, message);
    }
}
