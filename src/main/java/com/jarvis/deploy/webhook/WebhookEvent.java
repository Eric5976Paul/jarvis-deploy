package com.jarvis.deploy.webhook;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Represents an outbound webhook event triggered by a deployment lifecycle action.
 */
public class WebhookEvent {

    private final String eventId;
    private final String eventType;
    private final String environment;
    private final String applicationName;
    private final String version;
    private final Instant occurredAt;
    private final Map<String, String> metadata;

    public WebhookEvent(String eventId, String eventType, String environment,
                        String applicationName, String version,
                        Instant occurredAt, Map<String, String> metadata) {
        this.eventId = Objects.requireNonNull(eventId, "eventId must not be null");
        this.eventType = Objects.requireNonNull(eventType, "eventType must not be null");
        this.environment = Objects.requireNonNull(environment, "environment must not be null");
        this.applicationName = Objects.requireNonNull(applicationName, "applicationName must not be null");
        this.version = Objects.requireNonNull(version, "version must not be null");
        this.occurredAt = occurredAt != null ? occurredAt : Instant.now();
        this.metadata = metadata != null ? Collections.unmodifiableMap(new HashMap<>(metadata)) : Collections.emptyMap();
    }

    public String getEventId() { return eventId; }
    public String getEventType() { return eventType; }
    public String getEnvironment() { return environment; }
    public String getApplicationName() { return applicationName; }
    public String getVersion() { return version; }
    public Instant getOccurredAt() { return occurredAt; }
    public Map<String, String> getMetadata() { return metadata; }

    @Override
    public String toString() {
        return "WebhookEvent{" +
                "eventId='" + eventId + '\'' +
                ", eventType='" + eventType + '\'' +
                ", environment='" + environment + '\'' +
                ", applicationName='" + applicationName + '\'' +
                ", version='" + version + '\'' +
                ", occurredAt=" + occurredAt +
                '}';
    }
}
