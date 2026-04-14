package com.jarvis.deploy.dependency;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Captures the outcome of a dependency resolution check for a given deployment.
 */
public class DependencyCheckResult {

    private final String service;
    private final boolean satisfied;
    private final List<String> unsatisfiedDependencies;
    private final List<String> warnings;

    private DependencyCheckResult(String service, boolean satisfied,
                                   List<String> unsatisfiedDependencies, List<String> warnings) {
        this.service = service;
        this.satisfied = satisfied;
        this.unsatisfiedDependencies = Collections.unmodifiableList(unsatisfiedDependencies);
        this.warnings = Collections.unmodifiableList(warnings);
    }

    public static DependencyCheckResult success(String service, List<String> warnings) {
        return new DependencyCheckResult(service, true, Collections.emptyList(), warnings);
    }

    public static DependencyCheckResult failure(String service, List<String> unsatisfied, List<String> warnings) {
        Objects.requireNonNull(unsatisfied, "unsatisfied must not be null");
        if (unsatisfied.isEmpty()) {
            throw new IllegalArgumentException("Failure result must have at least one unsatisfied dependency");
        }
        return new DependencyCheckResult(service, false, unsatisfied, warnings);
    }

    public String getService() { return service; }
    public boolean isSatisfied() { return satisfied; }
    public List<String> getUnsatisfiedDependencies() { return unsatisfiedDependencies; }
    public List<String> getWarnings() { return warnings; }

    public boolean hasWarnings() { return !warnings.isEmpty(); }

    @Override
    public String toString() {
        return "DependencyCheckResult{service='" + service + "', satisfied=" + satisfied
                + ", unsatisfied=" + unsatisfiedDependencies + ", warnings=" + warnings + "}";
    }
}
