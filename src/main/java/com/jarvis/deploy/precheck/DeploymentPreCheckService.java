package com.jarvis.deploy.precheck;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Executes a suite of pre-deployment checks before a deployment proceeds.
 * Checks are registered by name and evaluated in registration order.
 */
public class DeploymentPreCheckService {

    private final List<RegisteredCheck> checks = new ArrayList<>();

    public void register(String name, PreCheckSeverity severity, Supplier<PreCheckResult> check) {
        Objects.requireNonNull(name, "Check name must not be null");
        Objects.requireNonNull(check, "Check supplier must not be null");
        checks.add(new RegisteredCheck(name, severity, check));
    }

    public PreCheckSuiteResult runAll(String environment, String version) {
        List<PreCheckResult> results = new ArrayList<>();
        boolean hasBlockingFailure = false;

        for (RegisteredCheck registered : checks) {
            PreCheckResult result;
            try {
                result = registered.check.get();
            } catch (Exception e) {
                result = PreCheckResult.builder(registered.name)
                        .passed(false)
                        .severity(registered.severity)
                        .message("Check threw exception: " + e.getMessage())
                        .build();
            }
            results.add(result);
            if (result.isBlockingFailure()) {
                hasBlockingFailure = true;
            }
        }

        return new PreCheckSuiteResult(environment, version, Collections.unmodifiableList(results), hasBlockingFailure);
    }

    public int getCheckCount() {
        return checks.size();
    }

    public void clear() {
        checks.clear();
    }

    private static class RegisteredCheck {
        final String name;
        final PreCheckSeverity severity;
        final Supplier<PreCheckResult> check;

        RegisteredCheck(String name, PreCheckSeverity severity, Supplier<PreCheckResult> check) {
            this.name = name;
            this.severity = severity;
            this.check = check;
        }
    }
}
