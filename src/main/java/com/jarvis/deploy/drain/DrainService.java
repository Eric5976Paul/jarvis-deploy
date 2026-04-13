package com.jarvis.deploy.drain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Service responsible for draining an application instance before deployment.
 * Uses a configurable readiness probe supplier to detect when traffic has drained.
 */
public class DrainService {

    private static final Logger log = LoggerFactory.getLogger(DrainService.class);
    private static final long POLL_INTERVAL_MS = 500;

    /** Supplier returns true when the instance has no active connections. */
    private final Supplier<Boolean> connectionIdleProbe;

    public DrainService(Supplier<Boolean> connectionIdleProbe) {
        this.connectionIdleProbe = connectionIdleProbe;
    }

    /**
     * Executes the drain operation described by the request.
     *
     * @param request the drain configuration
     * @return a DrainResult describing the outcome
     */
    public DrainResult drain(DrainRequest request) {
        log.info("Starting drain for app '{}' in environment '{}' (timeout={}s, force={})",
                request.getAppName(), request.getEnvironment(),
                request.getTimeout().toSeconds(), request.isForceOnTimeout());

        Instant start = Instant.now();
        long timeoutMs = request.getTimeout().toMillis();

        try {
            while (true) {
                if (connectionIdleProbe.get()) {
                    Duration elapsed = Duration.between(start, Instant.now());
                    log.info("Drain succeeded for '{}' in {}ms", request.getAppName(), elapsed.toMillis());
                    return DrainResult.drained(request.getEnvironment(), request.getAppName(), elapsed);
                }

                Duration elapsed = Duration.between(start, Instant.now());
                if (elapsed.toMillis() >= timeoutMs) {
                    log.warn("Drain timed out for '{}' after {}ms", request.getAppName(), elapsed.toMillis());
                    if (request.isForceOnTimeout()) {
                        log.warn("Force-stopping '{}' due to drain timeout", request.getAppName());
                        return DrainResult.forced(request.getEnvironment(), request.getAppName(), elapsed);
                    }
                    return DrainResult.timedOut(request.getEnvironment(), request.getAppName(), elapsed);
                }

                TimeUnit.MILLISECONDS.sleep(POLL_INTERVAL_MS);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Drain interrupted for '{}'", request.getAppName());
            return DrainResult.failed(request.getEnvironment(), request.getAppName(),
                    "Drain interrupted: " + e.getMessage());
        } catch (Exception e) {
            log.error("Drain failed for '{}': {}", request.getAppName(), e.getMessage());
            return DrainResult.failed(request.getEnvironment(), request.getAppName(), e.getMessage());
        }
    }
}
