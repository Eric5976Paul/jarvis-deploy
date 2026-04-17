package com.jarvis.deploy.alerting;

import java.util.Objects;
import java.util.function.Predicate;

public class AlertRule {
    private final String name;
    private final AlertSeverity severity;
    private final Predicate<String> condition;
    private final String messageTemplate;

    public AlertRule(String name, AlertSeverity severity, Predicate<String> condition, String messageTemplate) {
        this.name = Objects.requireNonNull(name);
        this.severity = Objects.requireNonNull(severity);
        this.condition = Objects.requireNonNull(condition);
        this.messageTemplate = Objects.requireNonNull(messageTemplate);
    }

    public String getName() { return name; }
    public AlertSeverity getSeverity() { return severity; }
    public String getMessageTemplate() { return messageTemplate; }

    public boolean matches(String context) {
        return condition.test(context);
    }
}
