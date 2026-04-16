package com.jarvis.deploy.label;

import java.util.*;
import java.util.stream.Collectors;

public class LabelRegistry {
    private final Map<String, DeploymentLabel> labels = new LinkedHashMap<>();

    public void put(DeploymentLabel label) {
        Objects.requireNonNull(label, "Label must not be null");
        labels.put(label.getKey(), label);
    }

    public Optional<DeploymentLabel> get(String key) {
        return Optional.ofNullable(labels.get(key));
    }

    public boolean remove(String key) {
        return labels.remove(key) != null;
    }

    public boolean hasLabel(String key) {
        return labels.containsKey(key);
    }

    public boolean hasLabelWithValue(String key, String value) {
        return get(key).map(l -> l.getValue().equals(value)).orElse(false);
    }

    public List<DeploymentLabel> all() {
        return Collections.unmodifiableList(new ArrayList<>(labels.values()));
    }

    public List<DeploymentLabel> propagatable() {
        return labels.values().stream()
                .filter(DeploymentLabel::isPropagatable)
                .collect(Collectors.toList());
    }

    public void merge(LabelRegistry other) {
        other.all().forEach(l -> labels.putIfAbsent(l.getKey(), l));
    }

    public int size() { return labels.size(); }

    public void clear() { labels.clear(); }
}
