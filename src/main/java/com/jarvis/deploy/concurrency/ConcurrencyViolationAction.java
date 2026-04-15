package com.jarvis.deploy.concurrency;

/**
 * Defines what action to take when the concurrency limit for deployments is exceeded.
 */
public enum ConcurrencyViolationAction {

    /** Immediately reject the incoming deployment request. */
    REJECT,

    /** Queue the deployment and wait up to the configured timeout. */
    QUEUE,

    /** Cancel the oldest running deployment and proceed with the new one. */
    CANCEL_OLDEST
}
