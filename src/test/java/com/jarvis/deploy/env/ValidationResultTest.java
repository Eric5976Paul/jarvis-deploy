package com.jarvis.deploy.env;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ValidationResultTest {

    @Test
    void validResultHasNoErrors() {
        ValidationResult result = new ValidationResult(true, List.of());
        assertTrue(result.isValid());
        assertTrue(result.getErrors().isEmpty());
    }

    @Test
    void invalidResultContainsErrors() {
        ValidationResult result = new ValidationResult(false, List.of("Error one", "Error two"));
        assertFalse(result.isValid());
        assertEquals(2, result.getErrors().size());
    }

    @Test
    void errorsListIsImmutable() {
        ValidationResult result = new ValidationResult(false, List.of("Some error"));
        assertThrows(UnsupportedOperationException.class, () -> result.getErrors().add("extra"));
    }

    @Test
    void nullErrorsListDefaultsToEmpty() {
        ValidationResult result = new ValidationResult(true, null);
        assertNotNull(result.getErrors());
        assertTrue(result.getErrors().isEmpty());
    }

    @Test
    void summaryContainsPassedWhenValid() {
        ValidationResult result = new ValidationResult(true, List.of());
        assertTrue(result.getSummary().contains("passed"));
    }

    @Test
    void summaryContainsErrorCountWhenInvalid() {
        ValidationResult result = new ValidationResult(false, List.of("err1", "err2"));
        String summary = result.getSummary();
        assertTrue(summary.contains("2"));
        assertTrue(summary.contains("failed"));
    }

    @Test
    void toStringContainsValidAndErrors() {
        ValidationResult result = new ValidationResult(false, List.of("bad config"));
        String str = result.toString();
        assertTrue(str.contains("valid=false"));
        assertTrue(str.contains("bad config"));
    }
}
