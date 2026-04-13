package com.jarvis.deploy.notification;

import com.jarvis.deploy.deployment.DeploymentRecord;
import com.jarvis.deploy.rollback.RollbackResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DeploymentNotifierTest {

    private DeploymentNotifier notifier;
    private CapturingChannel channel;

    @BeforeEach
    void setUp() {
        notifier = new DeploymentNotifier();
        channel = new CapturingChannel();
        notifier.registerChannel(channel);
    }

    @Test
    void notifyDeploymentStarted_shouldSendCorrectMessage() {
        notifier.notifyDeploymentStarted("staging", "1.2.0");

        assertEquals(1, channel.messages.size());
        assertTrue(channel.messages.get(0).contains("DEPLOY STARTED"));
        assertTrue(channel.messages.get(0).contains("staging"));
        assertTrue(channel.messages.get(0).contains("1.2.0"));
    }

    @Test
    void notifyDeploymentSuccess_shouldIncludeTimestamp() {
        DeploymentRecord record = new DeploymentRecord("prod", "2.0.0", LocalDateTime.now());

        notifier.notifyDeploymentSuccess(record);

        assertEquals(1, channel.messages.size());
        assertTrue(channel.messages.get(0).contains("DEPLOY SUCCESS"));
        assertTrue(channel.messages.get(0).contains("prod"));
        assertTrue(channel.messages.get(0).contains("2.0.0"));
    }

    @Test
    void notifyDeploymentFailure_shouldIncludeReason() {
        notifier.notifyDeploymentFailure("dev", "0.9.0", "health check timed out");

        assertEquals(1, channel.messages.size());
        String msg = channel.messages.get(0);
        assertTrue(msg.contains("DEPLOY FAILED"));
        assertTrue(msg.contains("health check timed out"));
    }

    @Test
    void notifyRollback_shouldReflectSuccessStatus() {
        RollbackResult result = RollbackResult.success("staging", "1.1.0", "Rolled back successfully");

        notifier.notifyRollback(result);

        assertEquals(1, channel.messages.size());
        assertTrue(channel.messages.get(0).contains("ROLLBACK SUCCESS"));
        assertTrue(channel.messages.get(0).contains("1.1.0"));
    }

    @Test
    void registerChannel_nullChannel_shouldThrow() {
        assertThrows(IllegalArgumentException.class, () -> notifier.registerChannel(null));
    }

    @Test
    void noChannels_shouldNotThrow() {
        DeploymentNotifier bare = new DeploymentNotifier();
        assertDoesNotThrow(() -> bare.notifyDeploymentStarted("qa", "1.0.0"));
    }

    // --- helper ---

    static class CapturingChannel implements NotificationChannel {
        final List<String> messages = new ArrayList<>();

        @Override
        public void send(String message) {
            messages.add(message);
        }
    }
}
