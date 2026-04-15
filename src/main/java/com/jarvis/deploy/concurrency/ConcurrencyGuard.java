package com.jarvis.deploy.concurrency;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * Enforces concurrency policies for deployments per environment.
 * Uses semaphores to track and limit active deployments.
 */
public class ConcurrencyGuard {

    private final Map<String, Semaphore> semaphores = new ConcurrentHashMap<>();
    private final Map<String, ConcurrencyPolicy> policies = new ConcurrentHashMap<>();

    public void registerPolicy(ConcurrencyPolicy policy) {
        policies.put(policy.getEnvironment(), policy);
        semaphores.put(policy.getEnvironment(),
                new Semaphore(policy.getMaxConcurrentDeployments(), true));
    }

    /**
     * Attempts to acquire a deployment slot for the given environment.
     *
     * @param environment the target environment
     * @return a {@link ConcurrencyAcquireResult} indicating success or failure
     */
    public ConcurrencyAcquireResult acquire(String environment) {
        ConcurrencyPolicy policy = policies.getOrDefault(
                environment, ConcurrencyPolicy.defaultPolicy(environment));

        Semaphore semaphore = semaphores.computeIfAbsent(environment,
                env -> new Semaphore(policy.getMaxConcurrentDeployments(), true));

        switch (policy.getViolationAction()) {
            case REJECT:
                if (semaphore.tryAcquire()) {
                    return ConcurrencyAcquireResult.acquired(environment);
                }
                return ConcurrencyAcquireResult.rejected(environment,
                        "Concurrency limit reached for environment: " + environment);

            case QUEUE:
                try {
                    boolean acquired = semaphore.tryAcquire(
                            policy.getQueueTimeoutSeconds(), TimeUnit.SECONDS);
                    if (acquired) {
                        return ConcurrencyAcquireResult.acquired(environment);
                    }
                    return ConcurrencyAcquireResult.rejected(environment,
                            "Timed out waiting for deployment slot in: " + environment);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return ConcurrencyAcquireResult.rejected(environment, "Interrupted while waiting");
                }

            case CANCEL_OLDEST:
                // Drain one permit forcibly to simulate cancelling oldest
                semaphore.drainPermits();
                semaphore.release(policy.getMaxConcurrentDeployments() - 1);
                return ConcurrencyAcquireResult.acquired(environment);

            default:
                return ConcurrencyAcquireResult.rejected(environment, "Unknown violation action");
        }
    }

    /**
     * Releases a previously acquired deployment slot.
     *
     * @param environment the target environment
     */
    public void release(String environment) {
        Semaphore semaphore = semaphores.get(environment);
        if (semaphore != null) {
            semaphore.release();
        }
    }

    public int availableSlots(String environment) {
        Semaphore semaphore = semaphores.get(environment);
        return semaphore != null ? semaphore.availablePermits() : -1;
    }
}
