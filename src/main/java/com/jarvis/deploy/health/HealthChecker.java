package com.jarvis.deploy.health;

import com.jarvis.deploy.config.DeploymentConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Duration;
import java.time.Instant;

/**
 * Performs HTTP health checks against a deployed Spring Boot application's
 * actuator endpoint to verify successful startup.
 */
public class HealthChecker {

    private static final Logger log = LoggerFactory.getLogger(HealthChecker.class);

    private static final String DEFAULT_HEALTH_PATH = "/actuator/health";
    private static final int DEFAULT_TIMEOUT_MS = 3000;

    private final int maxRetries;
    private final long retryDelayMs;
    private final int connectTimeoutMs;

    public HealthChecker(int maxRetries, long retryDelayMs) {
        this.maxRetries = maxRetries;
        this.retryDelayMs = retryDelayMs;
        this.connectTimeoutMs = DEFAULT_TIMEOUT_MS;
    }

    /**
     * Polls the health endpoint until the app responds UP or retries are exhausted.
     *
     * @param config the deployment config providing host/port information
     * @return a {@link HealthCheckResult} describing the outcome
     */
    public HealthCheckResult waitForHealthy(DeploymentConfig config) {
        String url = buildHealthUrl(config);
        log.info("Waiting for application to become healthy at {}", url);
        Instant start = Instant.now();

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            log.debug("Health check attempt {}/{}", attempt, maxRetries);
            try {
                int statusCode = doGet(url);
                if (statusCode == 200) {
                    Duration elapsed = Duration.between(start, Instant.now());
                    log.info("Application is healthy after {} ms", elapsed.toMillis());
                    return HealthCheckResult.success(url, elapsed);
                }
                log.warn("Health endpoint returned HTTP {}", statusCode);
            } catch (IOException e) {
                log.debug("Health check attempt {} failed: {}", attempt, e.getMessage());
            }

            if (attempt < maxRetries) {
                sleep(retryDelayMs);
            }
        }

        Duration elapsed = Duration.between(start, Instant.now());
        log.error("Application did not become healthy after {} attempts", maxRetries);
        return HealthCheckResult.failure(url, elapsed, maxRetries);
    }

    private String buildHealthUrl(DeploymentConfig config) {
        String host = config.getHost() != null ? config.getHost() : "localhost";
        int port = config.getPort() > 0 ? config.getPort() : 8080;
        return "http://" + host + ":" + port + DEFAULT_HEALTH_PATH;
    }

    private int doGet(String urlStr) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) new URL(urlStr).openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(connectTimeoutMs);
        conn.setReadTimeout(connectTimeoutMs);
        conn.connect();
        return conn.getResponseCode();
    }

    private void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
