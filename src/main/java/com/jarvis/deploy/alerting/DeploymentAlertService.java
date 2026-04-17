package com.jarvis.deploy.alerting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

public class DeploymentAlertService {
    private final List<AlertRule> rules = new CopyOnWriteArrayList<>();
    private final List<DeploymentAlert> alertHistory = new CopyOnWriteArrayList<>();

    public void registerRule(AlertRule rule) {
        if (rule == null) throw new IllegalArgumentException("Rule must not be null");
        rules.add(rule);
    }

    public List<DeploymentAlert> evaluate(String environment, String context) {
        List<DeploymentAlert> triggered = new ArrayList<>();
        for (AlertRule rule : rules) {
            if (rule.matches(context)) {
                String msg = rule.getMessageTemplate().replace("{context}", context);
                DeploymentAlert alert = new DeploymentAlert(
                    UUID.randomUUID().toString(), environment, rule.getSeverity(), msg
                );
                alertHistory.add(alert);
                triggered.add(alert);
            }
        }
        return Collections.unmodifiableList(triggered);
    }

    public List<DeploymentAlert> getHistory() {
        return Collections.unmodifiableList(alertHistory);
    }

    public List<DeploymentAlert> getHistoryByEnvironment(String environment) {
        List<DeploymentAlert> result = new ArrayList<>();
        for (DeploymentAlert a : alertHistory) {
            if (a.getEnvironment().equals(environment)) result.add(a);
        }
        return Collections.unmodifiableList(result);
    }

    public void clearHistory() {
        alertHistory.clear();
    }
}
