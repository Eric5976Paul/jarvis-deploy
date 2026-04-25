package com.jarvis.deploy.quarantine;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class QuarantineService {

    private final Map<String, QuarantineEntry> quarantined = new ConcurrentHashMap<>();

    public QuarantineEntry quarantine(String deploymentId, String environment, QuarantineReason reason, String details) {
        Objects.requireNonNull(deploymentId, "deploymentId must not be null");
        Objects.requireNonNull(environment, "environment must not be null");
        Objects.requireNonNull(reason, "reason must not be null");

        QuarantineEntry entry = new QuarantineEntry(deploymentId, environment, reason, details);
        quarantined.put(deploymentId, entry);
        return entry;
    }

    public boolean isQuarantined(String deploymentId) {
        QuarantineEntry entry = quarantined.get(deploymentId);
        return entry != null && !entry.isReleased();
    }

    public Optional<QuarantineEntry> getEntry(String deploymentId) {
        return Optional.ofNullable(quarantined.get(deploymentId));
    }

    public boolean release(String deploymentId) {
        QuarantineEntry entry = quarantined.get(deploymentId);
        if (entry == null || entry.isReleased()) {
            return false;
        }
        entry.release();
        return true;
    }

    public List<QuarantineEntry> getActiveQuarantines() {
        return quarantined.values().stream()
                .filter(e -> !e.isReleased())
                .collect(Collectors.toList());
    }

    public List<QuarantineEntry> getQuarantinesByEnvironment(String environment) {
        return quarantined.values().stream()
                .filter(e -> e.getEnvironment().equals(environment) && !e.isReleased())
                .collect(Collectors.toList());
    }

    public int purgeReleased() {
        List<String> releasedKeys = quarantined.entrySet().stream()
                .filter(e -> e.getValue().isReleased())
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        releasedKeys.forEach(quarantined::remove);
        return releasedKeys.size();
    }
}
