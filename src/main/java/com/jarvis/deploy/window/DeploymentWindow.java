package com.jarvis.deploy.window;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.EnumSet;
import java.util.Set;

/**
 * Represents a maintenance/deployment window defining allowed days and time ranges.
 */
public class DeploymentWindow {

    private final String name;
    private final LocalTime startTime;
    private final LocalTime endTime;
    private final Set<DayOfWeek> allowedDays;
    private final boolean active;

    public DeploymentWindow(String name, LocalTime startTime, LocalTime endTime,
                            Set<DayOfWeek> allowedDays, boolean active) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Window name must not be blank");
        }
        if (startTime == null || endTime == null) {
            throw new IllegalArgumentException("Start and end times must not be null");
        }
        if (!endTime.isAfter(startTime)) {
            throw new IllegalArgumentException("End time must be after start time");
        }
        this.name = name;
        this.startTime = startTime;
        this.endTime = endTime;
        this.allowedDays = allowedDays != null ? EnumSet.copyOf(allowedDays) : EnumSet.noneOf(DayOfWeek.class);
        this.active = active;
    }

    public boolean isOpen(DayOfWeek day, LocalTime time) {
        if (!active) return false;
        return allowedDays.contains(day) && !time.isBefore(startTime) && !time.isAfter(endTime);
    }

    public String getName() { return name; }
    public LocalTime getStartTime() { return startTime; }
    public LocalTime getEndTime() { return endTime; }
    public Set<DayOfWeek> getAllowedDays() { return EnumSet.copyOf(allowedDays); }
    public boolean isActive() { return active; }

    @Override
    public String toString() {
        return String.format("DeploymentWindow{name='%s', start=%s, end=%s, days=%s, active=%b}",
                name, startTime, endTime, allowedDays, active);
    }
}
