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
    CANCEL_OLDEST;

    /**
     * Returns whether this action allows the incoming deployment to eventually proceed.
     * <p>
     * {@code REJECT} discards the request entirely, while {@code QUEUE} and
     * {@code CANCEL_OLDEST} both result in the new deployment being executed.
     *
     * @return {@code true} if the deployment may proceed; {@code false} if it will be rejected
     */
    public boolean allowsDeploymentToproceed() {
        return this != REJECT;
    }
}
