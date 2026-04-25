package com.jarvis.deploy.delegation;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for delegating deployment permissions from one principal to another.
 * Supports time-bounded delegations with optional environment scope restrictions.
 */
public class DeploymentDelegationService {

    private final Map<String, DelegationRecord> activeDelegations = new ConcurrentHashMap<>();

    /**
     * Creates a new delegation from delegator to delegate.
     *
     * @param request the delegation request
     * @return the result of the delegation attempt
     */
    public DelegationResult delegate(DelegationRequest request) {
        if (request == null) {
            return DelegationResult.failure("Delegation request must not be null");
        }
        if (request.getDelegator() == null || request.getDelegator().isBlank()) {
            return DelegationResult.failure("Delegator must not be blank");
        }
        if (request.getDelegate() == null || request.getDelegate().isBlank()) {
            return DelegationResult.failure("Delegate must not be blank");
        }
        if (request.getDelegator().equals(request.getDelegate())) {
            return DelegationResult.failure("Delegator and delegate must be different principals");
        }
        if (request.getExpiresAt() != null && request.getExpiresAt().isBefore(Instant.now())) {
            return DelegationResult.failure("Delegation expiry must be in the future");
        }

        String key = buildKey(request.getDelegator(), request.getDelegate());
        DelegationRecord record = new DelegationRecord(
                UUID.randomUUID().toString(),
                request.getDelegator(),
                request.getDelegate(),
                request.getEnvironments(),
                Instant.now(),
                request.getExpiresAt()
        );
        activeDelegations.put(key, record);
        return DelegationResult.success(record.getDelegationId());
    }

    /**
     * Checks whether a delegation from delegator to delegate is currently active.
     *
     * @param delegator the original permission holder
     * @param delegate  the principal acting on behalf of the delegator
     * @param environment the environment context to validate against
     * @return true if a valid, unexpired delegation exists
     */
    public boolean isAuthorized(String delegator, String delegate, String environment) {
        String key = buildKey(delegator, delegate);
        DelegationRecord record = activeDelegations.get(key);
        if (record == null) {
            return false;
        }
        if (record.getExpiresAt() != null && record.getExpiresAt().isBefore(Instant.now())) {
            activeDelegations.remove(key);
            return false;
        }
        List<String> envs = record.getEnvironments();
        return envs == null || envs.isEmpty() || envs.contains(environment);
    }

    /**
     * Revokes an active delegation.
     *
     * @param delegator the original permission holder
     * @param delegate  the delegate whose access should be revoked
     * @return true if a delegation was found and removed
     */
    public boolean revoke(String delegator, String delegate) {
        return activeDelegations.remove(buildKey(delegator, delegate)) != null;
    }

    /**
     * Returns all currently active (non-expired) delegations.
     */
    public List<DelegationRecord> listActive() {
        Instant now = Instant.now();
        List<DelegationRecord> result = new ArrayList<>();
        activeDelegations.entrySet().removeIf(e -> {
            DelegationRecord r = e.getValue();
            return r.getExpiresAt() != null && r.getExpiresAt().isBefore(now);
        });
        result.addAll(activeDelegations.values());
        return Collections.unmodifiableList(result);
    }

    private String buildKey(String delegator, String delegate) {
        return delegator + "::" + delegate;
    }
}
