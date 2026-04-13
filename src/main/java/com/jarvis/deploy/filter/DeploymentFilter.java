package com.jarvis.deploy.filter;

import com.jarvis.deploy.deployment.DeploymentRecord;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Provides filtering capabilities for deployment records based on
 * various criteria such as environment, status, version, and time range.
 */
public class DeploymentFilter {

    private String environment;
    private String status;
    private String version;
    private Instant from;
    private Instant to;

    private DeploymentFilter() {}

    public static Builder builder() {
        return new Builder();
    }

    public List<DeploymentRecord> apply(List<DeploymentRecord> records) {
        if (records == null || records.isEmpty()) {
            return new ArrayList<>();
        }

        List<Predicate<DeploymentRecord>> predicates = new ArrayList<>();

        if (environment != null && !environment.isBlank()) {
            predicates.add(r -> environment.equalsIgnoreCase(r.getEnvironment()));
        }
        if (status != null && !status.isBlank()) {
            predicates.add(r -> status.equalsIgnoreCase(r.getStatus()));
        }
        if (version != null && !version.isBlank()) {
            predicates.add(r -> version.equals(r.getVersion()));
        }
        if (from != null) {
            predicates.add(r -> r.getTimestamp() != null && !r.getTimestamp().isBefore(from));
        }
        if (to != null) {
            predicates.add(r -> r.getTimestamp() != null && !r.getTimestamp().isAfter(to));
        }

        Predicate<DeploymentRecord> combined = predicates.stream()
                .reduce(x -> true, Predicate::and);

        return records.stream()
                .filter(combined)
                .collect(Collectors.toList());
    }

    public static class Builder {
        private final DeploymentFilter filter = new DeploymentFilter();

        public Builder environment(String environment) {
            filter.environment = environment;
            return this;
        }

        public Builder status(String status) {
            filter.status = status;
            return this;
        }

        public Builder version(String version) {
            filter.version = version;
            return this;
        }

        public Builder from(Instant from) {
            filter.from = from;
            return this;
        }

        public Builder to(Instant to) {
            filter.to = to;
            return this;
        }

        public DeploymentFilter build() {
            return filter;
        }
    }
}
