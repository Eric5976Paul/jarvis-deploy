package com.jarvis.deploy.diff;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Represents a diff between two deployment configurations or versions.
 */
public class DeploymentDiff {

    private final String fromVersion;
    private final String toVersion;
    private final String environment;
    private final Map<String, DiffEntry> changes;

    public DeploymentDiff(String fromVersion, String toVersion, String environment) {
        this.fromVersion = Objects.requireNonNull(fromVersion, "fromVersion must not be null");
        this.toVersion = Objects.requireNonNull(toVersion, "toVersion must not be null");
        this.environment = Objects.requireNonNull(environment, "environment must not be null");
        this.changes = new HashMap<>();
    }

    public void addChange(String key, String oldValue, String newValue) {
        changes.put(key, new DiffEntry(key, oldValue, newValue));
    }

    public boolean hasChanges() {
        return !changes.isEmpty();
    }

    public Map<String, DiffEntry> getChanges() {
        return Collections.unmodifiableMap(changes);
    }

    public String getFromVersion() {
        return fromVersion;
    }

    public String getToVersion() {
        return toVersion;
    }

    public String getEnvironment() {
        return environment;
    }

    public int getChangeCount() {
        return changes.size();
    }

    @Override
    public String toString() {
        return String.format("DeploymentDiff{from='%s', to='%s', env='%s', changes=%d}",
                fromVersion, toVersion, environment, changes.size());
    }
}
