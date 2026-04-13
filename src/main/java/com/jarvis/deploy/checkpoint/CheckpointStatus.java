package com.jarvis.deploy.checkpoint;

/**
 * Possible states of a deployment checkpoint.
 */
public enum CheckpointStatus {
    /** Checkpoint passed all criteria. */
    PASSED,
    /** Checkpoint failed one or more criteria. */
    FAILED,
    /** Checkpoint was skipped (e.g. not applicable for the environment). */
    SKIPPED
}
