package com.jarvis.deploy.correlation;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents a correlation context linking related deployment events
 * across services, environments, and pipeline stages.
 */
public class DeploymentCorrelation {

    private final String correlationId;
    private final String parentCorrelationId;
    private final String environment;
    private final String serviceName;
    private final String initiatedBy;
    private final Instant createdAt;

    public DeploymentCorrelation(String environment, String serviceName, String initiatedBy) {
        this(UUID.randomUUID().toString(), null, environment, serviceName, initiatedBy);
    }

    public DeploymentCorrelation(String correlationId, String parentCorrelationId,
                                  String environment, String serviceName, String initiatedBy) {
        this.correlationId = Objects.requireNonNull(correlationId, "correlationId must not be null");
        this.parentCorrelationId = parentCorrelationId;
        this.environment = Objects.requireNonNull(environment, "environment must not be null");
        this.serviceName = Objects.requireNonNull(serviceName, "serviceName must not be null");
        this.initiatedBy = Objects.requireNonNull(initiatedBy, "initiatedBy must not be null");
        this.createdAt = Instant.now();
    }

    public DeploymentCorrelation deriveChild() {
        return new DeploymentCorrelation(
                UUID.randomUUID().toString(),
                this.correlationId,
                this.environment,
                this.serviceName,
                this.initiatedBy
        );
    }

    public boolean hasParent() {
        return parentCorrelationId != null;
    }

    public String getCorrelationId() { return correlationId; }
    public String getParentCorrelationId() { return parentCorrelationId; }
    public String getEnvironment() { return environment; }
    public String getServiceName() { return serviceName; }
    public String getInitiatedBy() { return initiatedBy; }
    public Instant getCreatedAt() { return createdAt; }

    @Override
    public String toString() {
        return String.format("DeploymentCorrelation{id='%s', parent='%s', env='%s', service='%s'}",
                correlationId, parentCorrelationId, environment, serviceName);
    }
}
