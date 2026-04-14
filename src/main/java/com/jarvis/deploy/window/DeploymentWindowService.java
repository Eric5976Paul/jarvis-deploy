package com.jarvis.deploy.window;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Service for managing deployment windows and checking whether a deployment is currently permitted.
 */
public class DeploymentWindowService {

    private final List<DeploymentWindow> windows = new ArrayList<>();
    private boolean bypassEnabled = false;

    public void registerWindow(DeploymentWindow window) {
        if (window == null) {
            throw new IllegalArgumentException("Window must not be null");
        }
        windows.add(window);
    }

    public void enableBypass(boolean bypass) {
        this.bypassEnabled = bypass;
    }

    public WindowCheckResult checkNow(ZonedDateTime now) {
        if (bypassEnabled) {
            return WindowCheckResult.allowed("BYPASS", now);
        }
        if (windows.isEmpty()) {
            return WindowCheckResult.denied("No deployment windows configured", now);
        }
        for (DeploymentWindow window : windows) {
            if (window.isOpen(now.getDayOfWeek(), now.toLocalTime())) {
                return WindowCheckResult.allowed(window.getName(), now);
            }
        }
        return WindowCheckResult.denied(
                "Current time " + now + " is outside all configured deployment windows", now);
    }

    public WindowCheckResult checkAt(ZonedDateTime dateTime) {
        return checkNow(dateTime);
    }

    public List<DeploymentWindow> getWindows() {
        return Collections.unmodifiableList(windows);
    }

    public boolean isBypassEnabled() {
        return bypassEnabled;
    }

    public void clearWindows() {
        windows.clear();
    }
}
