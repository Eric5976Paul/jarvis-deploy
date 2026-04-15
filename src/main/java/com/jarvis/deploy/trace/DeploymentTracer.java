package com.jarvis.deploy.trace;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks distributed trace spans for a deployment lifecycle,
 * enabling end-to-end visibility across pipeline stages.
 */
public class DeploymentTracer {

    private final Map<String, List<TraceSpan>> traces = new ConcurrentHashMap<>();

    /**
     * Starts a new trace for the given deployment ID and returns the root span.
     */
    public TraceSpan startTrace(String deploymentId, String rootOperation) {
        if (deploymentId == null || deploymentId.isBlank()) {
            throw new IllegalArgumentException("deploymentId must not be blank");
        }
        TraceSpan root = TraceSpan.builder()
                .spanId(UUID.randomUUID().toString())
                .deploymentId(deploymentId)
                .operation(rootOperation)
                .startedAt(Instant.now())
                .build();
        traces.computeIfAbsent(deploymentId, k -> Collections.synchronizedList(new ArrayList<>())).add(root);
        return root;
    }

    /**
     * Adds a child span under an existing trace for the given deployment.
     */
    public TraceSpan addSpan(String deploymentId, String parentSpanId, String operation) {
        if (!traces.containsKey(deploymentId)) {
            throw new IllegalStateException("No active trace for deploymentId: " + deploymentId);
        }
        TraceSpan span = TraceSpan.builder()
                .spanId(UUID.randomUUID().toString())
                .deploymentId(deploymentId)
                .parentSpanId(parentSpanId)
                .operation(operation)
                .startedAt(Instant.now())
                .build();
        traces.get(deploymentId).add(span);
        return span;
    }

    /**
     * Marks a span as finished with the given status.
     */
    public void finishSpan(TraceSpan span, TraceSpanStatus status) {
        if (span == null) throw new IllegalArgumentException("span must not be null");
        span.finish(status, Instant.now());
    }

    /**
     * Returns all spans recorded for the given deployment ID.
     */
    public List<TraceSpan> getSpans(String deploymentId) {
        return Collections.unmodifiableList(
                traces.getOrDefault(deploymentId, Collections.emptyList()));
    }

    /**
     * Clears all trace data for the given deployment ID.
     */
    public void clearTrace(String deploymentId) {
        traces.remove(deploymentId);
    }

    /**
     * Returns true if a trace exists for the given deployment ID.
     */
    public boolean hasTrace(String deploymentId) {
        return traces.containsKey(deploymentId);
    }
}
