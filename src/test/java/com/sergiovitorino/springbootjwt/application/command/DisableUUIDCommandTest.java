package com.sergiovitorino.springbootjwt.application.command;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class DisableUUIDCommandTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void validate_shouldFailWhenIdIsNull() {
        var command = new DisableUUIDCommand(null);
        var violations = validator.validate(command);

        assertEquals(1, violations.size());
        assertEquals("Id not found", violations.iterator().next().getMessage());
    }

    @Test
    void validate_shouldPassWhenIdIsProvided() {
        var command = new DisableUUIDCommand(UUID.randomUUID());
        var violations = validator.validate(command);

        assertTrue(violations.isEmpty());
    }

    @Test
    void record_shouldExposeIdField() {
        UUID id = UUID.randomUUID();
        var command = new DisableUUIDCommand(id);

        assertEquals(id, command.id());
    }
}
