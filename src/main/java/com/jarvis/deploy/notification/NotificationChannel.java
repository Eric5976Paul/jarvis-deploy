package com.jarvis.deploy.notification;

/**
 * Contract for a notification delivery channel.
 * Implementations can target console output, HTTP webhooks, email, etc.
 */
public interface NotificationChannel {

    /**
     * Send a plain-text notification message.
     *
     * @param message the message to deliver
     */
    void send(String message);

    /**
     * Human-readable name for this channel, used in logs.
     */
    default String channelName() {
        return this.getClass().getSimpleName();
    }
}
