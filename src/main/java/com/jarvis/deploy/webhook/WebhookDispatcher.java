package com.jarvis.deploy.webhook;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Dispatches {@link WebhookEvent} payloads to configured webhook endpoints via HTTP POST.
 */
public class WebhookDispatcher {

    private static final Logger LOGGER = Logger.getLogger(WebhookDispatcher.class.getName());
    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(10);

    private final HttpClient httpClient;
    private final WebhookPayloadSerializer serializer;
    private final List<String> endpointUrls;

    public WebhookDispatcher(List<String> endpointUrls, WebhookPayloadSerializer serializer) {
        this.endpointUrls = Objects.requireNonNull(endpointUrls, "endpointUrls must not be null");
        this.serializer = Objects.requireNonNull(serializer, "serializer must not be null");
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(DEFAULT_TIMEOUT)
                .build();
    }

    /**
     * Dispatches the event to all configured endpoints.
     *
     * @param event the webhook event to dispatch
     * @return a summary result indicating overall success/failure
     */
    public WebhookDispatchResult dispatch(WebhookEvent event) {
        Objects.requireNonNull(event, "event must not be null");

        if (endpointUrls.isEmpty()) {
            LOGGER.fine("No webhook endpoints configured; skipping dispatch.");
            return WebhookDispatchResult.skipped(event.getEventId());
        }

        String payload = serializer.serialize(event);
        int successCount = 0;
        int failureCount = 0;

        for (String url : endpointUrls) {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .timeout(DEFAULT_TIMEOUT)
                        .header("Content-Type", "application/json")
                        .header("X-Jarvis-Event", event.getEventType())
                        .header("X-Jarvis-Event-Id", event.getEventId())
                        .POST(HttpRequest.BodyPublishers.ofString(payload))
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() >= 200 && response.statusCode() < 300) {
                    LOGGER.info("Webhook delivered to " + url + " [status=" + response.statusCode() + "]");
                    successCount++;
                } else {
                    LOGGER.warning("Webhook endpoint " + url + " returned status " + response.statusCode());
                    failureCount++;
                }
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Failed to deliver webhook to " + url, e);
                failureCount++;
            }
        }

        return WebhookDispatchResult.of(event.getEventId(), successCount, failureCount);
    }

    public List<String> getEndpointUrls() {
        return List.copyOf(endpointUrls);
    }
}
