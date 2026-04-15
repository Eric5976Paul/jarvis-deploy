package com.jarvis.deploy.webhook;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class WebhookDispatcherTest {

    private WebhookPayloadSerializer serializer;
    private WebhookEvent sampleEvent;

    @BeforeEach
    void setUp() {
        serializer = new WebhookPayloadSerializer();
        sampleEvent = new WebhookEvent(
                "evt-001",
                "DEPLOYMENT_STARTED",
                "production",
                "my-service",
                "1.4.2",
                Instant.parse("2024-06-01T12:00:00Z"),
                Map.of("triggeredBy", "ci-pipeline")
        );
    }

    @Test
    void dispatch_withNoEndpoints_returnsSkipped() {
        WebhookDispatcher dispatcher = new WebhookDispatcher(Collections.emptyList(), serializer);
        WebhookDispatchResult result = dispatcher.dispatch(sampleEvent);

        assertEquals(WebhookDispatchResult.Status.SKIPPED, result.getStatus());
        assertTrue(result.isSkipped());
        assertEquals("evt-001", result.getEventId());
    }

    @Test
    void dispatch_withInvalidUrl_returnsFailedResult() {
        WebhookDispatcher dispatcher = new WebhookDispatcher(
                List.of("http://localhost:0/nonexistent"), serializer);
        WebhookDispatchResult result = dispatcher.dispatch(sampleEvent);

        assertEquals(WebhookDispatchResult.Status.FAILED, result.getStatus());
        assertEquals(0, result.getSuccessCount());
        assertEquals(1, result.getFailureCount());
        assertFalse(result.isFullySuccessful());
    }

    @Test
    void dispatch_nullEvent_throwsNullPointerException() {
        WebhookDispatcher dispatcher = new WebhookDispatcher(Collections.emptyList(), serializer);
        assertThrows(NullPointerException.class, () -> dispatcher.dispatch(null));
    }

    @Test
    void getEndpointUrls_returnsImmutableCopy() {
        List<String> urls = List.of("http://example.com/hook1", "http://example.com/hook2");
        WebhookDispatcher dispatcher = new WebhookDispatcher(urls, serializer);
        List<String> returned = dispatcher.getEndpointUrls();
        assertEquals(2, returned.size());
        assertThrows(UnsupportedOperationException.class, () -> returned.add("http://extra.com"));
    }

    @Test
    void dispatchResult_partialStatus_whenMixedOutcomes() {
        WebhookDispatchResult result = WebhookDispatchResult.of("evt-002", 2, 1);
        assertEquals(WebhookDispatchResult.Status.PARTIAL, result.getStatus());
        assertEquals(2, result.getSuccessCount());
        assertEquals(1, result.getFailureCount());
        assertFalse(result.isFullySuccessful());
    }

    @Test
    void payloadSerializer_producesValidJsonStructure() {
        String payload = serializer.serialize(sampleEvent);
        assertTrue(payload.contains("\"eventId\":\"evt-001\""));
        assertTrue(payload.contains("\"eventType\":\"DEPLOYMENT_STARTED\""));
        assertTrue(payload.contains("\"environment\":\"production\""));
        assertTrue(payload.contains("\"version\":\"1.4.2\""));
        assertTrue(payload.contains("\"triggeredBy\":\"ci-pipeline\""));
    }
}
