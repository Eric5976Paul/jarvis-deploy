package com.jarvis.deploy.diff;

import java.util.Objects;

/**
 * Represents a single changed entry in a deployment diff.
 */
public class DiffEntry {

    public enum ChangeType {
        ADDED, REMOVED, MODIFIED
    }

    private final String key;
    private final String oldValue;
    private final String newValue;
    private final ChangeType changeType;

    public DiffEntry(String key, String oldValue, String newValue) {
        this.key = Objects.requireNonNull(key, "key must not be null");
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.changeType = resolveChangeType(oldValue, newValue);
    }

    private ChangeType resolveChangeType(String oldVal, String newVal) {
        if (oldVal == null) return ChangeType.ADDED;
        if (newVal == null) return ChangeType.REMOVED;
        return ChangeType.MODIFIED;
    }

    public String getKey() {
        return key;
    }

    public String getOldValue() {
        return oldValue;
    }

    public String getNewValue() {
        return newValue;
    }

    public ChangeType getChangeType() {
        return changeType;
    }

    @Override
    public String toString() {
        return String.format("DiffEntry{key='%s', type=%s, old='%s', new='%s'}",
                key, changeType, oldValue, newValue);
    }
}
