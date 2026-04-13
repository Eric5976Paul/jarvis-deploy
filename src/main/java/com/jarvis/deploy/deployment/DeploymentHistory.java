package com.jarvis.deploy.deployment;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Manages deployment history persistence and retrieval for rollback support.
 */
public class DeploymentHistory {

    private static final String HISTORY_FILE = ".jarvis/deployment-history.log";
    private final List<DeploymentRecord> records = new ArrayList<>();
    private final Path historyPath;

    public DeploymentHistory() {
        this(HISTORY_FILE);
    }

    public DeploymentHistory(String historyFilePath) {
        this.historyPath = Paths.get(historyFilePath);
        load();
    }

    public void record(DeploymentRecord entry) {
        records.add(entry);
        persist(entry);
    }

    public List<DeploymentRecord> getHistory(String environment) {
        return records.stream()
                .filter(r -> r.getEnvironment().equalsIgnoreCase(environment))
                .sorted(Comparator.comparing(DeploymentRecord::getDeployedAt).reversed())
                .collect(Collectors.toList());
    }

    public Optional<DeploymentRecord> getLastSuccessful(String environment) {
        return getHistory(environment).stream()
                .filter(r -> r.getStatus() == DeploymentRecord.DeploymentStatus.SUCCESS)
                .findFirst();
    }

    private void load() {
        if (!Files.exists(historyPath)) return;
        try (BufferedReader reader = Files.newBufferedReader(historyPath)) {
            String line;
            while ((line = reader.readLine()) != null) {
                parseRecord(line).ifPresent(records::add);
            }
        } catch (IOException e) {
            System.err.println("Warning: Could not load deployment history: " + e.getMessage());
        }
    }

    private void persist(DeploymentRecord record) {
        try {
            Files.createDirectories(historyPath.getParent());
            String line = String.join("|", record.getEnvironment(), record.getVersion(),
                    record.getArtifactPath(), record.getDeployedAt().toString(),
                    record.getStatus().name());
            Files.writeString(historyPath, line + System.lineSeparator(),
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            System.err.println("Warning: Could not persist deployment record: " + e.getMessage());
        }
    }

    private Optional<DeploymentRecord> parseRecord(String line) {
        String[] parts = line.split("\\|");
        if (parts.length != 5) return Optional.empty();
        try {
            return Optional.of(new DeploymentRecord(
                    parts[0], parts[1], parts[2],
                    LocalDateTime.parse(parts[3]),
                    DeploymentRecord.DeploymentStatus.valueOf(parts[4])));
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
