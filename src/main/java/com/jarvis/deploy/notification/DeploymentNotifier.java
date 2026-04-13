package com.jarvis.deploy.notification;

import com.jarvis.deploy.deployment.DeploymentRecord;
import com.jarvis.deploy.rollback.RollbackResult;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Sends deployment event notifications to registered channels (e.g. console, webhook).
 */
public class DeploymentNotifier {

    private static final Logger logger = Logger.getLogger(DeploymentNotifier.class.getName());
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final List<NotificationChannel> channels = new ArrayList<>();

    public void registerChannel(NotificationChannel channel) {
        if (channel == null) {
            throw new IllegalArgumentException("Notification channel must not be null");
        }
        channels.add(channel);
    }

    public void notifyDeploymentStarted(String environment, String version) {
        String message = String.format("[DEPLOY STARTED] env=%s version=%s", environment, version);
        dispatch(message);
    }

    public void notifyDeploymentSuccess(DeploymentRecord record) {
        String message = String.format(
                "[DEPLOY SUCCESS] env=%s version=%s deployedAt=%s",
                record.getEnvironment(),
                record.getVersion(),
                record.getDeployedAt().format(FORMATTER)
        );
        dispatch(message);
    }

    public void notifyDeploymentFailure(String environment, String version, String reason) {
        String message = String.format(
                "[DEPLOY FAILED] env=%s version=%s reason=%s",
                environment, version, reason
        );
        dispatch(message);
    }

    public void notifyRollback(RollbackResult result) {
        String status = result.isSuccess() ? "SUCCESS" : "FAILED";
        String message = String.format(
                "[ROLLBACK %s] env=%s targetVersion=%s details=%s",
                status,
                result.getEnvironment(),
                result.getTargetVersion(),
                result.getMessage()
        );
        dispatch(message);
    }

    private void dispatch(String message) {
        if (channels.isEmpty()) {
            logger.info("No notification channels registered. Message: " + message);
            return;
        }
        for (NotificationChannel channel : channels) {
            try {
                channel.send(message);
            } catch (Exception e) {
                logger.warning("Failed to send notification via " + channel.getClass().getSimpleName() + ": " + e.getMessage());
            }
        }
    }
}
