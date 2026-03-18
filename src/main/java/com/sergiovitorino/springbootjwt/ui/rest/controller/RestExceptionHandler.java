package com.sergiovitorino.springbootjwt.ui.rest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sergiovitorino.springbootjwt.domain.exception.BusinessException;
import com.sergiovitorino.springbootjwt.domain.exception.EmailAlreadyExistsException;
import com.sergiovitorino.springbootjwt.domain.exception.ResourceNotFoundException;
import com.sergiovitorino.springbootjwt.infrastructure.ErrorBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class RestExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(RestExceptionHandler.class);

    private final ObjectMapper mapper;

    public RestExceptionHandler(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<String> handleResourceNotFound(ResourceNotFoundException exception) throws Exception {
        return buildErrorResponse(HttpStatus.NOT_FOUND, "NOT_FOUND", exception);
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<String> handleEmailAlreadyExists(EmailAlreadyExistsException exception) throws Exception {
        return buildErrorResponse(HttpStatus.UNPROCESSABLE_ENTITY, "EMAIL_ALREADY_EXISTS", exception);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<String> handleBusinessException(BusinessException exception) throws Exception {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "BUSINESS_ERROR", exception);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<String> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException exception) throws Exception {
        log.warn("Invalid argument type: {}", exception.getMessage());
        List<ErrorBean> errors = Collections.singletonList(new ErrorBean("INVALID_ARGUMENT", null, "Invalid UUID format"));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(mapper.writeValueAsString(errors));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<String> handleMethodArgumentNotValid(MethodArgumentNotValidException exception) throws Exception {
        List<ErrorBean> errors = exception.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> new ErrorBean(null, fieldError.getField(), fieldError.getDefaultMessage()))
                .collect(Collectors.toList());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(mapper.writeValueAsString(errors));
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<String> handleHandlerMethodValidation(HandlerMethodValidationException exception) throws Exception {
        // Para validação de parâmetros de método (não @RequestBody)
        List<ErrorBean> errors = exception.getAllValidationResults().stream()
                .flatMap(result -> result.getResolvableErrors().stream())
                .map(error -> new ErrorBean(null, null, error.getDefaultMessage()))
                .collect(Collectors.toList());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(mapper.writeValueAsString(errors));
    }

    @ExceptionHandler
    public ResponseEntity<String> exceptionHandler(final Exception exception) throws Exception {
        log.error("Unhandled exception occurred: {}", exception.getMessage(), exception);
        List<ErrorBean> errors = Collections.singletonList(new ErrorBean("InternalServerError", null, "An unexpected error occurred"));
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(mapper.writeValueAsString(errors));
    }

    private ResponseEntity<String> buildErrorResponse(HttpStatus status, String errorCode, Exception exception) throws Exception {
        List<ErrorBean> errors = Collections.singletonList(new ErrorBean(errorCode, null, exception.getMessage()));
        return ResponseEntity.status(status).body(mapper.writeValueAsString(errors));
    }

}
