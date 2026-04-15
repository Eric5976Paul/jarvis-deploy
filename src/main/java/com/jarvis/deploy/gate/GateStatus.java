package com.jarvis.deploy.gate;

/**
 * Represents the evaluation status of a deployment gate.
 */
public enum GateStatus {
    /** Gate has not yet been evaluated. */
    PENDING,
    /** Gate conditions were met and deployment may proceed. */
    PASSED,
    /** Gate conditions were not met; deployment is blocked. */
    FAILED
}
