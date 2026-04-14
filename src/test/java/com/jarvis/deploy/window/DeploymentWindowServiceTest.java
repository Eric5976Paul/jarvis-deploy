package com.jarvis.deploy.window;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.EnumSet;

import static org.junit.jupiter.api.Assertions.*;

class DeploymentWindowServiceTest {

    private DeploymentWindowService service;

    @BeforeEach
    void setUp() {
        service = new DeploymentWindowService();
    }

    private DeploymentWindow weekdayBusinessHours() {
        return new DeploymentWindow(
                "business-hours",
                LocalTime.of(9, 0),
                LocalTime.of(17, 0),
                EnumSet.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                        DayOfWeek.THURSDAY, DayOfWeek.FRIDAY),
                true);
    }

    private ZonedDateTime at(DayOfWeek day, int hour, int minute) {
        // Find next occurrence of the given day from a fixed reference
        ZonedDateTime base = ZonedDateTime.of(2024, 1, 1, hour, minute, 0, 0, ZoneId.of("UTC"));
        while (base.getDayOfWeek() != day) base = base.plusDays(1);
        return base;
    }

    @Test
    void checkNow_noWindowsConfigured_returnsDenied() {
        WindowCheckResult result = service.checkNow(ZonedDateTime.now());
        assertFalse(result.isPermitted());
        assertTrue(result.getReason().contains("No deployment windows"));
    }

    @Test
    void checkNow_withinWindow_returnsAllowed() {
        service.registerWindow(weekdayBusinessHours());
        ZonedDateTime tuesday10am = at(DayOfWeek.TUESDAY, 10, 30);
        WindowCheckResult result = service.checkNow(tuesday10am);
        assertTrue(result.isPermitted());
        assertEquals("business-hours", result.getWindowName());
    }

    @Test
    void checkNow_outsideWindowTime_returnsDenied() {
        service.registerWindow(weekdayBusinessHours());
        ZonedDateTime tuesday8am = at(DayOfWeek.TUESDAY, 8, 0);
        WindowCheckResult result = service.checkNow(tuesday8am);
        assertFalse(result.isPermitted());
    }

    @Test
    void checkNow_weekend_returnsDenied() {
        service.registerWindow(weekdayBusinessHours());
        ZonedDateTime saturday = at(DayOfWeek.SATURDAY, 12, 0);
        WindowCheckResult result = service.checkNow(saturday);
        assertFalse(result.isPermitted());
    }

    @Test
    void checkNow_bypassEnabled_alwaysAllowed() {
        service.enableBypass(true);
        ZonedDateTime saturday = at(DayOfWeek.SATURDAY, 3, 0);
        WindowCheckResult result = service.checkNow(saturday);
        assertTrue(result.isPermitted());
        assertEquals("BYPASS", result.getWindowName());
    }

    @Test
    void registerWindow_nullThrows() {
        assertThrows(IllegalArgumentException.class, () -> service.registerWindow(null));
    }

    @Test
    void deploymentWindow_invalidTimes_throws() {
        assertThrows(IllegalArgumentException.class, () ->
                new DeploymentWindow("bad", LocalTime.of(17, 0), LocalTime.of(9, 0),
                        EnumSet.of(DayOfWeek.MONDAY), true));
    }

    @Test
    void getWindows_returnsUnmodifiableList() {
        service.registerWindow(weekdayBusinessHours());
        assertThrows(UnsupportedOperationException.class, () ->
                service.getWindows().add(weekdayBusinessHours()));
    }

    @Test
    void clearWindows_removesAll() {
        service.registerWindow(weekdayBusinessHours());
        service.clearWindows();
        assertTrue(service.getWindows().isEmpty());
    }
}
