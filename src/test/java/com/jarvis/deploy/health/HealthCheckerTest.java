package com.jarvis.deploy.health;

import com.jarvis.deploy.config.DeploymentConfig;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

import static org.junit.jupiter.api.Assertions.*;

class HealthCheckerTest {

    private HttpServer server;
    private int port;

    @BeforeEach
    void setUp() throws IOException {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        port = server.getAddress().getPort();
    }

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.stop(0);
        }
    }

    private DeploymentConfig configFor(int port) {
        DeploymentConfig config = new DeploymentConfig();
        config.setHost("localhost");
        config.setPort(port);
        return config;
    }

    @Test
    void returnsSuccess_whenEndpointResponds200() throws IOException {
        server.createContext("/actuator/health", exchange -> {
            byte[] body = "{\"status\":\"UP\"}".getBytes();
            exchange.sendResponseHeaders(200, body.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(body);
            }
        });
        server.start();

        HealthChecker checker = new HealthChecker(3, 100);
        HealthCheckResult result = checker.waitForHealthy(configFor(port));

        assertTrue(result.isHealthy());
        assertEquals(HealthCheckResult.Status.SUCCESS, result.getStatus());
        assertNotNull(result.getElapsed());
    }

    @Test
    void returnsFailure_whenEndpointResponds500() throws IOException {
        server.createContext("/actuator/health", exchange -> {
            exchange.sendResponseHeaders(500, 0);
            exchange.getResponseBody().close();
        });
        server.start();

        HealthChecker checker = new HealthChecker(2, 50);
        HealthCheckResult result = checker.waitForHealthy(configFor(port));

        assertFalse(result.isHealthy());
        assertEquals(HealthCheckResult.Status.FAILURE, result.getStatus());
        assertEquals(2, result.getAttemptsUsed());
    }

    @Test
    void returnsFailure_whenNoServerAvailable() {
        // Use a port with no server listening
        HealthChecker checker = new HealthChecker(2, 50);
        HealthCheckResult result = checker.waitForHealthy(configFor(19999));

        assertFalse(result.isHealthy());
        assertEquals(HealthCheckResult.Status.FAILURE, result.getStatus());
    }

    @Test
    void toStringContainsRelevantInfo() {
        HealthCheckResult result = HealthCheckResult.failure("http://localhost:8080/actuator/health",
                java.time.Duration.ofMillis(500), 3);
        String str = result.toString();
        assertTrue(str.contains("FAILURE"));
        assertTrue(str.contains("localhost"));
        assertTrue(str.contains("3"));
    }
}
