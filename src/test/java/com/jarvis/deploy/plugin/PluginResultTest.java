package com.jarvis.deploy.plugin;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PluginResultTest {

    @Test
    void ok_noArgs_shouldReturnSuccessWithDefaultMessage() {
        PluginResult result = PluginResult.ok();
        assertTrue(result.isSuccess());
        assertNotNull(result.getMessage());
        assertFalse(result.shouldHaltPipeline());
    }

    @Test
    void ok_withMessage_shouldReturnSuccessWithGivenMessage() {
        PluginResult result = PluginResult.ok("Custom success");
        assertTrue(result.isSuccess());
        assertEquals("Custom success", result.getMessage());
        assertFalse(result.shouldHaltPipeline());
    }

    @Test
    void failure_shouldReturnFailureWithoutHalt() {
        PluginResult result = PluginResult.failure("Something went wrong");
        assertFalse(result.isSuccess());
        assertEquals("Something went wrong", result.getMessage());
        assertFalse(result.shouldHaltPipeline());
    }

    @Test
    void fatalFailure_shouldReturnFailureWithHalt() {
        PluginResult result = PluginResult.fatalFailure("Critical error");
        assertFalse(result.isSuccess());
        assertEquals("Critical error", result.getMessage());
        assertTrue(result.shouldHaltPipeline());
    }

    @Test
    void ok_nullMessage_shouldThrowNullPointerException() {
        assertThrows(NullPointerException.class, () -> PluginResult.ok(null));
    }

    @Test
    void failure_nullMessage_shouldThrowNullPointerException() {
        assertThrows(NullPointerException.class, () -> PluginResult.failure(null));
    }

    @Test
    void toString_shouldContainKeyFields() {
        PluginResult result = PluginResult.fatalFailure("halt now");
        String str = result.toString();
        assertTrue(str.contains("halt now"));
        assertTrue(str.contains("haltPipeline=true"));
        assertTrue(str.contains("success=false"));
    }
}
