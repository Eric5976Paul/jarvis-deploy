package com.jarvis.deploy.baseline;

import java.util.Collections;
import java.util.List;

/**
 * Encapsulates the result of a baseline drift detection check.
 */
public class BaselineDriftResult {

    private final String environment;
    private final String baselineId;
    private final List<String> driftedKeys;
    private final boolean baselineFound;

    private BaselineDriftResult(String environment, String baselineId,
                                 List<String> driftedKeys, boolean baselineFound) {
        this.environment = environment;
        this.baselineId = baselineId;
        this.driftedKeys = Collections.unmodifiableList(driftedKeys);
        this.baselineFound = baselineFound;
    }

    public static BaselineDriftResult noBaseline(String environment) {
        return new BaselineDriftResult(environment, null, Collections.emptyList(), false);
    }

    public static BaselineDriftResult of(String environment, String baselineId, List<String> driftedKeys) {
        return new BaselineDriftResult(environment, baselineId, driftedKeys, true);
    }

    public boolean isBaselineFound() { return baselineFound; }

    public boolean hasDrift() {
        return baselineFound && !driftedKeys.isEmpty();
    }

    public String getEnvironment() { return environment; }
    public String getBaselineId() { return baselineId; }
    public List<String> getDriftedKeys() { return driftedKeys; }
    public int getDriftCount() { return driftedKeys.size(); }

    @Override
    public String toString() {
        if (!baselineFound) {
            return "BaselineDriftResult{environment='" + environment + "', noBaselineFound}";
        }
        return "BaselineDriftResult{environment='" + environment + "', baselineId='" + baselineId +
               "', driftedKeys=" + driftedKeys + ", hasDrift=" + hasDrift() + "}";
    }
}
