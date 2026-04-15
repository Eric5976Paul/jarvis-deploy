package com.jarvis.deploy.webhook;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * Serializes a {@link WebhookEvent} into a JSON string payload without external dependencies.
 */
public class WebhookPayloadSerializer {

    public String serialize(WebhookEvent event) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append(jsonField("eventId", event.getEventId())).append(",");
        sb.append(jsonField("eventType", event.getEventType())).append(",");
        sb.append(jsonField("environment", event.getEnvironment())).append(",");
        sb.append(jsonField("applicationName", event.getApplicationName())).append(",");
        sb.append(jsonField("version", event.getVersion())).append(",");
        sb.append(jsonField("occurredAt", event.getOccurredAt().toString())).append(",");
        sb.append("\"metadata\":").append(serializeMetadata(event.getMetadata()));
        sb.append("}");
        return sb.toString();
    }

    private String jsonField(String key, String value) {
        return "\"" + escapeJson(key) + "\":\"" + escapeJson(value) + "\"";
    }

    private String serializeMetadata(Map<String, String> metadata) {
        if (metadata == null || metadata.isEmpty()) {
            return "{}";
        }
        String entries = metadata.entrySet().stream()
                .map(e -> jsonField(e.getKey(), e.getValue()))
                .collect(Collectors.joining(","));
        return "{" + entries + "}";
    }

    private String escapeJson(String value) {
        if (value == null) return "";
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
