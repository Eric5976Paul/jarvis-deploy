package com.jarvis.deploy.dependency;

import java.time.Instant;
import java.util.Objects;

/**
 * Represents a dependency relationship between two deployments.
 * A deployment may require another service/version to be deployed first.
 */
public class DeploymentDependency {

    private final String dependentService;
    private final String requiredService;
    private final String requiredVersion;
    private final DependencyType type;
    private final Instant registeredAt;

    public DeploymentDependency(String dependentService, String requiredService,
                                String requiredVersion, DependencyType type) {
        this.dependentService = Objects.requireNonNull(dependentService, "dependentService must not be null");
        this.requiredService = Objects.requireNonNull(requiredService, "requiredService must not be null");
        this.requiredVersion = Objects.requireNonNull(requiredVersion, "requiredVersion must not be null");
        this.type = Objects.requireNonNull(type, "type must not be null");
        this.registeredAt = Instant.now();
    }

    public String getDependentService() { return dependentService; }
    public String getRequiredService() { return requiredService; }
    public String getRequiredVersion() { return requiredVersion; }
    public DependencyType getType() { return type; }
    public Instant getRegisteredAt() { return registeredAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DeploymentDependency)) return false;
        DeploymentDependency that = (DeploymentDependency) o;
        return Objects.equals(dependentService, that.dependentService)
                && Objects.equals(requiredService, that.requiredService)
                && Objects.equals(requiredVersion, that.requiredVersion);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dependentService, requiredService, requiredVersion);
    }

    @Override
    public String toString() {
        return "DeploymentDependency{" + dependentService + " -> " + requiredService + ":" + requiredVersion + ", type=" + type + "}";
    }
}
