package com.jarvis.deploy.env;

import java.util.Collections;
import java.util.List;

/**
 * Represents the outcome of an environment validation check.
 */
public class ValidationResult {

    private final boolean valid;
    private final List<String> errors;

    public ValidationResult(boolean valid, List<String> errors) {
        this.valid = valid;
        this.errors = errors != null ? List.copyOf(errors) : Collections.emptyList();
    }

    public boolean isValid() {
        return valid;
    }

    public List<String> getErrors() {
        return errors;
    }

    public String getSummary() {
        if (valid) {
            return "Environment validation passed.";
        }
        return "Environment validation failed with " + errors.size() + " error(s): " + String.join("; ", errors);
    }

    @Override
    public String toString() {
        return "ValidationResult{valid=" + valid + ", errors=" + errors + "}";
    }
}
