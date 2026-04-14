package com.jarvis.deploy.window;

import java.time.ZonedDateTime;

/**
 * Result of a deployment window check indicating whether a deployment is permitted.
 */
public class WindowCheckResult {

    private final boolean permitted;
    private final String windowName;
    private final String reason;
    private final ZonedDateTime checkedAt;

    private WindowCheckResult(boolean permitted, String windowName, String reason, ZonedDateTime checkedAt) {
        this.permitted = permitted;
        this.windowName = windowName;
        this.reason = reason;
        this.checkedAt = checkedAt;
    }

    public static WindowCheckResult allowed(String windowName, ZonedDateTime checkedAt) {
        return new WindowCheckResult(true, windowName,
                "Deployment is within the allowed window: " + windowName, checkedAt);
    }

    public static WindowCheckResult denied(String reason, ZonedDateTime checkedAt) {
        return new WindowCheckResult(false, null, reason, checkedAt);
    }

    public boolean isPermitted() { return permitted; }
    public String getWindowName() { return windowName; }
    public String getReason() { return reason; }
    public ZonedDateTime getCheckedAt() { return checkedAt; }

    @Override
    public String toString() {
        return String.format("WindowCheckResult{permitted=%b, window='%s', reason='%s'}",
                permitted, windowName, reason);
    }
}
