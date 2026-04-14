package com.jarvis.deploy.dependency;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Resolves and validates deployment dependencies before a deployment is executed.
 * Dependencies are registered per service and checked against a set of currently
 * deployed services and their versions.
 */
public class DependencyResolver {

    private final Map<String, List<DeploymentDependency>> dependencyRegistry = new ConcurrentHashMap<>();

    /**
     * Registers a dependency for a service.
     */
    public void register(DeploymentDependency dependency) {
        dependencyRegistry
                .computeIfAbsent(dependency.getDependentService(), k -> new ArrayList<>())
                .add(dependency);
    }

    /**
     * Returns all registered dependencies for the given service.
     */
    public List<DeploymentDependency> getDependencies(String service) {
        return Collections.unmodifiableList(
                dependencyRegistry.getOrDefault(service, Collections.emptyList()));
    }

    /**
     * Checks whether all dependencies for the given service are satisfied.
     *
     * @param service        the service about to be deployed
     * @param deployedServices a map of currently deployed service -> version
     * @return a {@link DependencyCheckResult} describing the outcome
     */
    public DependencyCheckResult check(String service, Map<String, String> deployedServices) {
        List<DeploymentDependency> deps = getDependencies(service);
        if (deps.isEmpty()) {
            return DependencyCheckResult.success(service, Collections.emptyList());
        }

        List<String> unsatisfied = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        for (DeploymentDependency dep : deps) {
            String deployedVersion = deployedServices.get(dep.getRequiredService());
            boolean present = deployedVersion != null;

            switch (dep.getType()) {
                case HARD:
                    if (!present) {
                        unsatisfied.add(dep.getRequiredService() + ":" + dep.getRequiredVersion() + " (HARD)");
                    }
                    break;
                case SOFT:
                    if (!present) {
                        warnings.add(dep.getRequiredService() + ":" + dep.getRequiredVersion() + " not deployed (SOFT — proceeding)");
                    }
                    break;
                case VERSION_CONSTRAINT:
                    if (!present) {
                        unsatisfied.add(dep.getRequiredService() + ":" + dep.getRequiredVersion() + " (VERSION_CONSTRAINT — not found)");
                    } else if (!meetsVersionConstraint(deployedVersion, dep.getRequiredVersion())) {
                        unsatisfied.add(dep.getRequiredService() + " requires >= " + dep.getRequiredVersion() + ", found " + deployedVersion);
                    }
                    break;
                default:
                    warnings.add("Unknown dependency type for " + dep.getRequiredService());
            }
        }

        if (!unsatisfied.isEmpty()) {
            return DependencyCheckResult.failure(service, unsatisfied, warnings);
        }
        return DependencyCheckResult.success(service, warnings);
    }

    /**
     * Naive semver-style check: deployed >= required based on string comparison of numeric parts.
     */
    private boolean meetsVersionConstraint(String deployed, String required) {
        try {
            String[] d = deployed.split("\\.");
            String[] r = required.split("\\.");
            int len = Math.max(d.length, r.length);
            for (int i = 0; i < len; i++) {
                int dv = i < d.length ? Integer.parseInt(d[i]) : 0;
                int rv = i < r.length ? Integer.parseInt(r[i]) : 0;
                if (dv != rv) return dv > rv;
            }
            return true;
        } catch (NumberFormatException e) {
            return deployed.compareTo(required) >= 0;
        }
    }

    public void clear() {
        dependencyRegistry.clear();
    }
}
