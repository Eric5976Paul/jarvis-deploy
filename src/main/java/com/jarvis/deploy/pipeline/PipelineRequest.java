package com.jarvis.deploy.pipeline;

/**
 * Encapsulates the parameters required to initiate a deployment pipeline run.
 */
public class PipelineRequest {

    private final String environment;
    private final String artifactId;
    private final String version;
    private final String initiator;

    private PipelineRequest(Builder builder) {
        this.environment = builder.environment;
        this.artifactId = builder.artifactId;
        this.version = builder.version;
        this.initiator = builder.initiator;
    }

    public String getEnvironment() { return environment; }
    public String getArtifactId() { return artifactId; }
    public String getVersion() { return version; }
    public String getInitiator() { return initiator; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String environment;
        private String artifactId;
        private String version;
        private String initiator = "system";

        public Builder environment(String environment) { this.environment = environment; return this; }
        public Builder artifactId(String artifactId) { this.artifactId = artifactId; return this; }
        public Builder version(String version) { this.version = version; return this; }
        public Builder initiator(String initiator) { this.initiator = initiator; return this; }

        public PipelineRequest build() {
            if (environment == null || environment.isBlank()) throw new IllegalArgumentException("environment is required");
            if (artifactId == null || artifactId.isBlank()) throw new IllegalArgumentException("artifactId is required");
            if (version == null || version.isBlank()) throw new IllegalArgumentException("version is required");
            return new PipelineRequest(this);
        }
    }

    @Override
    public String toString() {
        return "PipelineRequest{env='" + environment + "', artifact='" + artifactId + "', version='" + version + "', initiator='" + initiator + "'}";
    }
}
