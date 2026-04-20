package com.jarvis.deploy.precheck;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents the result of a pre-deployment check execution.
 */
public class PreCheckResult {

    private final String checkName;
    private final boolean passed;
    private final List<String> messages;
    private final PreCheckSeverity severity;

    private PreCheckResult(Builder builder) {
        this.checkName = builder.checkName;
        this.passed = builder.passed;
        this.messages = Collections.unmodifiableList(new ArrayList<>(builder.messages));
        this.severity = builder.severity;
    }

    public String getCheckName() {
        return checkName;
    }

    public boolean isPassed() {
        return passed;
    }

    public List<String> getMessages() {
        return messages;
    }

    public PreCheckSeverity getSeverity() {
        return severity;
    }

    public boolean isBlockingFailure() {
        return !passed && severity == PreCheckSeverity.CRITICAL;
    }

    public static Builder builder(String checkName) {
        return new Builder(checkName);
    }

    public static class Builder {
        private final String checkName;
        private boolean passed = true;
        private final List<String> messages = new ArrayList<>();
        private PreCheckSeverity severity = PreCheckSeverity.WARNING;

        private Builder(String checkName) {
            this.checkName = checkName;
        }

        public Builder passed(boolean passed) {
            this.passed = passed;
            return this;
        }

        public Builder message(String message) {
            this.messages.add(message);
            return this;
        }

        public Builder severity(PreCheckSeverity severity) {
            this.severity = severity;
            return this;
        }

        public PreCheckResult build() {
            return new PreCheckResult(this);
        }
    }

    @Override
    public String toString() {
        return "PreCheckResult{checkName='" + checkName + "', passed=" + passed +
                ", severity=" + severity + ", messages=" + messages + "}";
    }
}
