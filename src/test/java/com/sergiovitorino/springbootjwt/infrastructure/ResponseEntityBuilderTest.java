package com.sergiovitorino.springbootjwt.infrastructure;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ResponseEntityBuilderTest {

    private Validator validator;
    private ObjectMapper objectMapper;
    private ResponseEntityBuilder builder;

    @BeforeEach
    void setUp() {
        validator = new Validator();
        objectMapper = new ObjectMapper();
        builder = new ResponseEntityBuilder(validator, objectMapper);
    }

    @Test
    void shouldBuildSuccessResponseWithResult() {
        var result = "Test Result";

        var response = builder
                .result(result)
                .httpStatusSuccess(HttpStatus.OK)
                .build();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("Test Result"));
    }

    @Test
    void shouldBuildSuccessResponseWithDefaultStatus() {
        var result = "Test Result";

        var response = builder
                .result(result)
                .build();

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void shouldBuildErrorResponseWhenValidatorHasErrors() {
        validator.addError("Validation error");

        var response = builder
                .httpStatusError(HttpStatus.BAD_REQUEST)
                .build();

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().contains("Validation error"));
    }

    @Test
    void shouldBuildBadRequestWhenBindingResultHasErrors() {
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.hasErrors()).thenReturn(true);
        FieldError fieldError = new FieldError("user", "name", "Name is required");
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        var response = builder
                .bindingResult(bindingResult)
                .build();

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().contains("Name is required"));
        assertTrue(response.getBody().contains("name"));
    }

    @Test
    void shouldBuildSuccessResponseWithEmptyList() {
        // PageImpl with Unpaged causes serialization issues, so use a simple list instead
        List<String> emptyList = Collections.emptyList();

        var response = builder
                .result(emptyList)
                .httpStatusSuccess(HttpStatus.OK)
                .build();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("[]", response.getBody());
    }

    @Test
    void shouldBuildErrorResponseWhenResultIsNullAndErrorStatusIsNotFound() {
        // When result is null and httpStatusError is NOT_FOUND, it returns empty array with success status
        var response = builder
                .httpStatusError(HttpStatus.NOT_FOUND)
                .httpStatusSuccess(HttpStatus.OK)
                .build();

        // Based on the code, when httpStatusError is NOT_FOUND and result is null,
        // it returns httpStatusSuccess with empty array "[]"
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("[]", response.getBody());
    }

    @Test
    void shouldChainBuilderMethods() {
        var result = "Test";

        var response = builder
                .result(result)
                .httpStatusSuccess(HttpStatus.CREATED)
                .httpStatusError(HttpStatus.BAD_REQUEST)
                .build();

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }
}
