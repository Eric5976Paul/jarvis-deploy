package com.jarvis.deploy.cli;

import com.jarvis.deploy.config.ConfigLoader;
import com.jarvis.deploy.config.DeploymentConfig;
import com.jarvis.deploy.deployment.DeploymentService;
import com.jarvis.deploy.health.HealthChecker;
import com.jarvis.deploy.health.HealthCheckResult;
import com.jarvis.deploy.rollback.RollbackResult;
import com.jarvis.deploy.rollback.RollbackService;

import java.util.Arrays;
import java.util.List;

/**
 * Dispatches CLI commands to the appropriate service.
 */
public class CommandDispatcher {

    private final ConfigLoader configLoader;
    private final DeploymentService deploymentService;
    private final RollbackService rollbackService;
    private final HealthChecker healthChecker;

    public CommandDispatcher(ConfigLoader configLoader,
                             DeploymentService deploymentService,
                             RollbackService rollbackService,
                             HealthChecker healthChecker) {
        this.configLoader = configLoader;
        this.deploymentService = deploymentService;
        this.rollbackService = rollbackService;
        this.healthChecker = healthChecker;
    }

    public CliResult dispatch(String[] args) {
        if (args == null || args.length == 0) {
            return CliResult.failure("No command provided. Use: deploy, rollback, health, history");
        }

        String command = args[0].toLowerCase();
        List<String> params = args.length > 1 ? Arrays.asList(Arrays.copyOfRange(args, 1, args.length)) : List.of();

        try {
            return switch (command) {
                case "deploy" -> handleDeploy(params);
                case "rollback" -> handleRollback(params);
                case "health" -> handleHealth(params);
                case "history" -> handleHistory(params);
                default -> CliResult.failure("Unknown command: " + command);
            };
        } catch (Exception e) {
            return CliResult.failure("Command failed: " + e.getMessage());
        }
    }

    private CliResult handleDeploy(List<String> params) {
        if (params.size() < 2) {
            return CliResult.failure("Usage: deploy <environment> <version>");
        }
        String env = params.get(0);
        String version = params.get(1);
        DeploymentConfig config = configLoader.load();
        deploymentService.deploy(env, version, config);
        return CliResult.success("Deployed version " + version + " to " + env);
    }

    private CliResult handleRollback(List<String> params) {
        if (params.isEmpty()) {
            return CliResult.failure("Usage: rollback <environment>");
        }
        String env = params.get(0);
        RollbackResult result = rollbackService.rollback(env);
        return result.isSuccess()
                ? CliResult.success(result.getMessage())
                : CliResult.failure(result.getMessage());
    }

    private CliResult handleHealth(List<String> params) {
        if (params.isEmpty()) {
            return CliResult.failure("Usage: health <environment>");
        }
        String env = params.get(0);
        DeploymentConfig config = configLoader.load();
        HealthCheckResult result = healthChecker.check(env, config);
        return result.isHealthy()
                ? CliResult.success("Health OK for " + env + ": " + result.getDetails())
                : CliResult.failure("Unhealthy: " + result.getDetails());
    }

    private CliResult handleHistory(List<String> params) {
        if (params.isEmpty()) {
            return CliResult.failure("Usage: history <environment>");
        }
        String env = params.get(0);
        String history = deploymentService.getHistory(env);
        return CliResult.success(history);
    }
}
