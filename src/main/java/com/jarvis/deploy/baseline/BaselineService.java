package com.jarvis.deploy.baseline;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Service responsible for capturing, storing, and comparing deployment baselines.
 * Supports drift detection by comparing current deployment state against a stored baseline.
 */
public class BaselineService {

    private final Map<String, DeploymentBaseline> store = new ConcurrentHashMap<>();

    public DeploymentBaseline capture(String environment, String version,
                                       Map<String, String> properties, String capturedBy) {
        Objects.requireNonNull(environment, "environment must not be null");
        Objects.requireNonNull(version, "version must not be null");
        String id = UUID.randomUUID().toString();
        DeploymentBaseline baseline = new DeploymentBaseline(
                id, environment, version, Instant.now(),
                properties != null ? properties : Collections.emptyMap(), capturedBy);
        store.put(buildKey(environment), baseline);
        return baseline;
    }

    public Optional<DeploymentBaseline> getBaseline(String environment) {
        return Optional.ofNullable(store.get(buildKey(environment)));
    }

    public boolean hasBaseline(String environment) {
        return store.containsKey(buildKey(environment));
    }

    public BaselineDriftResult detectDrift(String environment, Map<String, String> currentProperties) {
        Optional<DeploymentBaseline> baselineOpt = getBaseline(environment);
        if (baselineOpt.isEmpty()) {
            return BaselineDriftResult.noBaseline(environment);
        }
        DeploymentBaseline baseline = baselineOpt.get();
        Map<String, String> baselineProps = baseline.getProperties();
        Map<String, String> current = currentProperties != null ? currentProperties : Collections.emptyMap();

        List<String> driftedKeys = new ArrayList<>();
        Set<String> allKeys = new HashSet<>();
        allKeys.addAll(baselineProps.keySet());
        allKeys.addAll(current.keySet());

        for (String key : allKeys) {
            String baselineVal = baselineProps.get(key);
            String currentVal = current.get(key);
            if (!Objects.equals(baselineVal, currentVal)) {
                driftedKeys.add(key);
            }
        }
        return BaselineDriftResult.of(environment, baseline.getBaselineId(), driftedKeys);
    }

    public boolean removeBaseline(String environment) {
        return store.remove(buildKey(environment)) != null;
    }

    public List<DeploymentBaseline> listAll() {
        return store.values().stream()
                .sorted(Comparator.comparing(DeploymentBaseline::getCapturedAt).reversed())
                .collect(Collectors.toList());
    }

    private String buildKey(String environment) {
        return environment.toLowerCase(Locale.ROOT);
    }
}
