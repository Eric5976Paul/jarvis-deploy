package com.jarvis.deploy.lock;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * Manages deployment locks to prevent concurrent deployments to the same environment.
 */
public class DeploymentLockManager {

    private static final Logger logger = Logger.getLogger(DeploymentLockManager.class.getName());
    private static final String LOCK_DIR = ".jarvis/locks";

    private final ConcurrentHashMap<String, LockEntry> activeLocks = new ConcurrentHashMap<>();
    private final String lockDirectory;

    public DeploymentLockManager() {
        this(LOCK_DIR);
    }

    public DeploymentLockManager(String lockDirectory) {
        this.lockDirectory = lockDirectory;
    }

    public LockAcquisitionResult acquireLock(String environment) {
        if (environment == null || environment.isBlank()) {
            return LockAcquisitionResult.failure("Environment name must not be null or blank");
        }

        if (activeLocks.containsKey(environment)) {
            LockEntry existing = activeLocks.get(environment);
            return LockAcquisitionResult.failure(
                String.format("Environment '%s' is already locked since %s", environment, existing.getAcquiredAt())
            );
        }

        try {
            Path lockDir = Paths.get(lockDirectory);
            Files.createDirectories(lockDir);
            Path lockFile = lockDir.resolve(environment + ".lock");

            FileChannel channel = FileChannel.open(lockFile,
                StandardOpenOption.CREATE, StandardOpenOption.WRITE);
            FileLock fileLock = channel.tryLock();

            if (fileLock == null) {
                channel.close();
                return LockAcquisitionResult.failure(
                    String.format("Could not acquire file lock for environment '%s'", environment)
                );
            }

            LockEntry entry = new LockEntry(environment, fileLock, channel, Instant.now());
            activeLocks.put(environment, entry);
            logger.info("Lock acquired for environment: " + environment);
            return LockAcquisitionResult.success(environment);

        } catch (IOException e) {
            return LockAcquisitionResult.failure(
                "Failed to acquire lock for environment '" + environment + "': " + e.getMessage()
            );
        }
    }

    public boolean releaseLock(String environment) {
        LockEntry entry = activeLocks.remove(environment);
        if (entry == null) {
            logger.warning("No active lock found for environment: " + environment);
            return false;
        }
        try {
            entry.getFileLock().release();
            entry.getChannel().close();
            Path lockFile = Paths.get(lockDirectory).resolve(environment + ".lock");
            Files.deleteIfExists(lockFile);
            logger.info("Lock released for environment: " + environment);
            return true;
        } catch (IOException e) {
            logger.severe("Failed to release lock for environment '" + environment + "': " + e.getMessage());
            return false;
        }
    }

    public boolean isLocked(String environment) {
        return activeLocks.containsKey(environment);
    }
}
