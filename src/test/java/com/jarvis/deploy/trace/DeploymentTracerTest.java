package com.jarvis.deploy.trace;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DeploymentTracerTest {

    private DeploymentTracer tracer;

    @BeforeEach
    void setUp() {
        tracer = new DeploymentTracer();
    }

    @Test
    void startTrace_createsRootSpanForDeployment() {
        TraceSpan root = tracer.startTrace("deploy-001", "pipeline.start");

        assertNotNull(root);
        assertEquals("deploy-001", root.getDeploymentId());
        assertEquals("pipeline.start", root.getOperation());
        assertNotNull(root.getSpanId());
        assertNull(root.getParentSpanId());
        assertNotNull(root.getStartedAt());
        assertTrue(tracer.hasTrace("deploy-001"));
    }

    @Test
    void startTrace_throwsOnBlankDeploymentId() {
        assertThrows(IllegalArgumentException.class, () -> tracer.startTrace("", "op"));
        assertThrows(IllegalArgumentException.class, () -> tracer.startTrace(null, "op"));
    }

    @Test
    void addSpan_appendsChildSpanUnderExistingTrace() {
        TraceSpan root = tracer.startTrace("deploy-002", "pipeline.start");
        TraceSpan child = tracer.addSpan("deploy-002", root.getSpanId(), "health.check");

        assertNotNull(child);
        assertEquals(root.getSpanId(), child.getParentSpanId());
        assertEquals("health.check", child.getOperation());

        List<TraceSpan> spans = tracer.getSpans("deploy-002");
        assertEquals(2, spans.size());
    }

    @Test
    void addSpan_throwsWhenNoActiveTrace() {
        assertThrows(IllegalStateException.class,
                () -> tracer.addSpan("unknown-deploy", "parent-id", "some.op"));
    }

    @Test
    void finishSpan_setsStatusAndFinishedAt() {
        TraceSpan root = tracer.startTrace("deploy-003", "pipeline.start");
        assertNull(root.getFinishedAt());

        tracer.finishSpan(root, TraceSpanStatus.SUCCESS);

        assertEquals(TraceSpanStatus.SUCCESS, root.getStatus());
        assertNotNull(root.getFinishedAt());
    }

    @Test
    void finishSpan_throwsOnNullSpan() {
        assertThrows(IllegalArgumentException.class, () -> tracer.finishSpan(null, TraceSpanStatus.FAILURE));
    }

    @Test
    void clearTrace_removesAllSpansForDeployment() {
        tracer.startTrace("deploy-004", "pipeline.start");
        assertTrue(tracer.hasTrace("deploy-004"));

        tracer.clearTrace("deploy-004");

        assertFalse(tracer.hasTrace("deploy-004"));
        assertEquals(0, tracer.getSpans("deploy-004").size());
    }

    @Test
    void getSpans_returnsUnmodifiableList() {
        tracer.startTrace("deploy-005", "pipeline.start");
        List<TraceSpan> spans = tracer.getSpans("deploy-005");

        assertThrows(UnsupportedOperationException.class, () -> spans.add(null));
    }
}
