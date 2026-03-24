package com.sergiovitorino.springbootjwt.ui.rest.controller.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sergiovitorino.springbootjwt.domain.exception.BusinessException;
import com.sergiovitorino.springbootjwt.domain.exception.EmailAlreadyExistsException;
import com.sergiovitorino.springbootjwt.domain.exception.ResourceNotFoundException;
import com.sergiovitorino.springbootjwt.infrastructure.ErrorBean;
import com.sergiovitorino.springbootjwt.ui.rest.controller.RestExceptionHandler;
import org.junit.jupiter.api.Test;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class RestExceptionHandlerTest {

    private final ObjectMapper mapper = new ObjectMapper();
    private final RestExceptionHandler handler = new RestExceptionHandler(mapper);

    @Test
    public void testIfExceptionParserIsOk() throws Exception {
        var responseEntity = handler.exceptionHandler(new IllegalArgumentException("Mock Exception"));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
        assertFalse(responseEntity.getBody().contains("IllegalArgumentException"));
        assertTrue(responseEntity.getBody().contains("InternalServerError"));
    }

    @Test
    public void testIfResourceNotFoundReturns404WithErrorCode() throws Exception {
        var responseEntity = handler.handleBusinessException(new ResourceNotFoundException("User not found"));
        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());

        List<ErrorBean> errors = mapper.readValue(responseEntity.getBody(), mapper.getTypeFactory().constructParametricType(List.class, ErrorBean.class));
        assertEquals("NOT_FOUND", errors.getFirst().errorCode());
        assertEquals("User not found", errors.getFirst().message());
        assertFalse(responseEntity.getBody().contains("com.sergiovitorino"));
    }

    @Test
    public void testIfEmailAlreadyExistsReturns422WithErrorCode() throws Exception {
        var responseEntity = handler.handleBusinessException(new EmailAlreadyExistsException("E-mail already"));
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, responseEntity.getStatusCode());

        List<ErrorBean> errors = mapper.readValue(responseEntity.getBody(), mapper.getTypeFactory().constructParametricType(List.class, ErrorBean.class));
        assertEquals("EMAIL_ALREADY_EXISTS", errors.getFirst().errorCode());
    }

    @Test
    public void testIfBusinessExceptionDefaultReturns400() throws Exception {
        // Direct BusinessException (not a subclass) hits the default branch
        var responseEntity = handler.handleBusinessException(new BusinessException("Invalid operation"));
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());

        List<ErrorBean> errors = mapper.readValue(responseEntity.getBody(), mapper.getTypeFactory().constructParametricType(List.class, ErrorBean.class));
        assertEquals("BUSINESS_ERROR", errors.getFirst().errorCode());
    }

    @Test
    public void testIfGeneric500DoesNotLeakDetails() throws Exception {
        var responseEntity = handler.exceptionHandler(new RuntimeException("sensitive internal detail"));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
        assertFalse(responseEntity.getBody().contains("sensitive internal detail"));
        assertTrue(responseEntity.getBody().contains("An unexpected error occurred"));
    }
}
