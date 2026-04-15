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

    /**
     * Returns a new {@code ValidationResult} representing a successful validation
     * with no errors.
     *
     * @return a valid {@code ValidationResult}
     */
    public static ValidationResult success() {
        return new ValidationResult(true, Collections.emptyList());
    }

    /**
     * Returns a new {@code ValidationResult} representing a failed validation
     * with the given list of error messages.
     *
     * @param errors the list of error messages describing why validation failed
     * @return an invalid {@code ValidationResult}
     */
    public static ValidationResult failure(List<String> errors) {
        return new ValidationResult(false, errors);
    }

    @Override
    public String toString() {
        return "ValidationResult{valid=" + valid + ", errors=" + errors + "}";
    }
}
