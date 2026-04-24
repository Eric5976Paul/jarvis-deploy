package com.jarvis.deploy.compliancy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Evaluates all registered compliance rules against a deployment context.
 * Rules with BLOCKING severity prevent the deployment from proceeding.
 */
public class ComplianceCheckService {

    private final List<RegisteredRule> registeredRules = new ArrayList<>();

    /**
     * Register a compliance rule along with its evaluator predicate.
     *
     * @param rule      the compliance rule metadata
     * @param evaluator predicate that receives deployment context variables and returns true when compliant
     */
    public void register(ComplianceRule rule, Predicate<Map<String, String>> evaluator) {
        registeredRules.add(new RegisteredRule(rule, evaluator));
    }

    /**
     * Run all applicable rules for the given environment and context.
     *
     * @param environment the target deployment environment
     * @param context     key-value deployment context (e.g. artifact version, requester, etc.)
     * @return a {@link ComplianceCheckResult} summarising all violations
     */
    public ComplianceCheckResult check(String environment, Map<String, String> context) {
        List<ComplianceViolation> violations = registeredRules.stream()
                .filter(r -> r.rule.appliesToEnvironment(environment))
                .filter(r -> !r.evaluator.test(context))
                .map(r -> new ComplianceViolation(r.rule, buildMessage(r.rule, context)))
                .collect(Collectors.toList());

        boolean blocked = violations.stream()
                .anyMatch(v -> v.getRule().getSeverity() == ComplianceRule.ComplianceSeverity.BLOCKING);

        return new ComplianceCheckResult(environment, violations, blocked);
    }

    public List<ComplianceRule> listRules() {
        return registeredRules.stream().map(r -> r.rule).collect(Collectors.toList());
    }

    private String buildMessage(ComplianceRule rule, Map<String, String> context) {
        return "Compliance rule '" + rule.getRuleId() + "' failed: " + rule.getDescription();
    }

    private static class RegisteredRule {
        final ComplianceRule rule;
        final Predicate<Map<String, String>> evaluator;

        RegisteredRule(ComplianceRule rule, Predicate<Map<String, String>> evaluator) {
            this.rule = rule;
            this.evaluator = evaluator;
        }
    }

    // Inner value types for results
    public static class ComplianceViolation {
        private final ComplianceRule rule;
        private final String message;

        public ComplianceViolation(ComplianceRule rule, String message) {
            this.rule = rule;
            this.message = message;
        }

        public ComplianceRule getRule() { return rule; }
        public String getMessage() { return message; }
    }

    public static class ComplianceCheckResult {
        private final String environment;
        private final List<ComplianceViolation> violations;
        private final boolean blocked;

        public ComplianceCheckResult(String environment, List<ComplianceViolation> violations, boolean blocked) {
            this.environment = environment;
            this.violations = Collections.unmodifiableList(violations);
            this.blocked = blocked;
        }

        public String getEnvironment() { return environment; }
        public List<ComplianceViolation> getViolations() { return violations; }
        public boolean isBlocked() { return blocked; }
        public boolean isPassed() { return !blocked; }
        public int violationCount() { return violations.size(); }
    }
}
