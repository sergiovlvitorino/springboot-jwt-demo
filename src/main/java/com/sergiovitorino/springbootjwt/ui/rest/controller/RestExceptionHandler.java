package com.sergiovitorino.springbootjwt.ui.rest.controller;

import java.util.Collections;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sergiovitorino.springbootjwt.domain.exception.EmailAlreadyExistsException;
import com.sergiovitorino.springbootjwt.domain.exception.ResourceNotFoundException;
import com.sergiovitorino.springbootjwt.infrastructure.ErrorBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;

@RestControllerAdvice
public class RestExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(RestExceptionHandler.class);

    private final ObjectMapper mapper;

    public RestExceptionHandler(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<String> handleResourceNotFound(ResourceNotFoundException exception) throws Exception {
        return buildErrorResponse(HttpStatus.NOT_FOUND, "NOT_FOUND", exception.getMessage());
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<String> handleEmailAlreadyExists(EmailAlreadyExistsException exception) throws Exception {
        return buildErrorResponse(HttpStatus.UNPROCESSABLE_ENTITY, "EMAIL_ALREADY_EXISTS", exception.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<String> handleAccessDenied(AccessDeniedException exception) throws Exception {
        List<ErrorBean> errors = Collections.singletonList(new ErrorBean("ACCESS_DENIED", null, "You don't have permission to perform this action"));
        return new ResponseEntity<>(mapper.writeValueAsString(errors), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<String> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException exception) throws Exception {
        log.warn("Invalid argument type: {}", exception.getMessage());
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "INVALID_ARGUMENT", "Invalid UUID format");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<String> handleMethodArgumentNotValid(MethodArgumentNotValidException exception) throws Exception {
        var errors = exception.getBindingResult().getAllErrors().stream()
                .map(error -> new ErrorBean(error.getCode(), error.getObjectName(), error.getDefaultMessage()))
                .toList();
        return new ResponseEntity<>(mapper.writeValueAsString(errors), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> exceptionHandler(Exception exception) throws Exception {
        log.error("Unexpected error", exception);
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "InternalServerError", "An unexpected error occurred");
    }

    private ResponseEntity<String> buildErrorResponse(HttpStatus status, String errorCode, String message) throws Exception {
        List<ErrorBean> errors = List.of(new ErrorBean(errorCode, null, message));
        return new ResponseEntity<>(mapper.writeValueAsString(errors), status);
    }
}
