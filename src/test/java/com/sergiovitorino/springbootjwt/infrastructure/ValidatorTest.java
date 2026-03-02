package com.sergiovitorino.springbootjwt.infrastructure;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ValidatorTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        validator = new Validator();
    }

    @Test
    void shouldStartWithNoErrors() {
        assertFalse(validator.isInvalid());
        assertTrue(validator.getErrors().isEmpty());
    }

    @Test
    void shouldAddError() {
        validator.addError("Test error message");

        assertTrue(validator.isInvalid());
        assertEquals(1, validator.getErrors().size());
        assertEquals("Test error message", validator.getErrors().get(0).message());
    }

    @Test
    void shouldAddMultipleErrors() {
        validator.addError("Error 1");
        validator.addError("Error 2");
        validator.addError("Error 3");

        assertTrue(validator.isInvalid());
        assertEquals(3, validator.getErrors().size());
    }

    @Test
    void shouldClearErrors() {
        validator.addError("Error 1");
        validator.addError("Error 2");
        assertTrue(validator.isInvalid());

        validator.clearErrors();

        assertFalse(validator.isInvalid());
        assertTrue(validator.getErrors().isEmpty());
    }

    @Test
    void shouldReturnCorrectErrorsList() {
        validator.addError("First error");
        validator.addError("Second error");

        var errors = validator.getErrors();

        assertEquals(2, errors.size());
        assertEquals("First error", errors.get(0).message());
        assertEquals("Second error", errors.get(1).message());
        assertNull(errors.get(0).className());
        assertNull(errors.get(0).fieldError());
    }
}
