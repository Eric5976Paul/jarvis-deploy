package com.jarvis.deploy.audit;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Logs audit events to an in-memory store and optionally to a file.
 */
public class AuditLogger {

    private final List<AuditEvent> events = new CopyOnWriteArrayList<>();
    private final Path logFilePath;

    public AuditLogger() {
        this.logFilePath = null;
    }

    public AuditLogger(Path logFilePath) {
        this.logFilePath = logFilePath;
    }

    public void log(AuditEvent event) {
        if (event == null) return;
        events.add(event);
        if (logFilePath != null) {
            persistToFile(event);
        }
    }

    public void log(AuditEvent.EventType type, String environment, String version, String actor, String message) {
        log(new AuditEvent(type, environment, version, actor, message));
    }

    public List<AuditEvent> getEvents() {
        return Collections.unmodifiableList(new ArrayList<>(events));
    }

    public List<AuditEvent> getEventsForEnvironment(String environment) {
        List<AuditEvent> result = new ArrayList<>();
        for (AuditEvent e : events) {
            if (e.getEnvironment().equals(environment)) {
                result.add(e);
            }
        }
        return Collections.unmodifiableList(result);
    }

    public void clear() {
        events.clear();
    }

    private void persistToFile(AuditEvent event) {
        try {
            if (logFilePath.getParent() != null) {
                Files.createDirectories(logFilePath.getParent());
            }
            try (PrintWriter writer = new PrintWriter(new FileWriter(logFilePath.toFile(), true))) {
                writer.println(event.toString());
            }
        } catch (IOException e) {
            System.err.println("[AuditLogger] Failed to write audit event to file: " + e.getMessage());
        }
    }
}
