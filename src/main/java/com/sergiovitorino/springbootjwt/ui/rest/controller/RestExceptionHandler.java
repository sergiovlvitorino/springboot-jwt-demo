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

import java.util.List;

@RestControllerAdvice
public class RestExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(RestExceptionHandler.class);

    private final ObjectMapper mapper;

    public RestExceptionHandler(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<String> handleBusinessException(BusinessException exception) throws Exception {
        var result = switch (exception) {
            case ResourceNotFoundException e -> new ErrorResult(HttpStatus.NOT_FOUND, "NOT_FOUND", e.getMessage());
            case EmailAlreadyExistsException e -> new ErrorResult(HttpStatus.UNPROCESSABLE_ENTITY, "EMAIL_ALREADY_EXISTS", e.getMessage());
            default -> new ErrorResult(HttpStatus.BAD_REQUEST, "BUSINESS_ERROR", exception.getMessage());
        };
        return buildErrorResponse(result.status(), result.errorCode(), result.message());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<String> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException exception) throws Exception {
        log.warn("Invalid argument type: {}", exception.getMessage());
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "INVALID_ARGUMENT", "Invalid UUID format");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<String> handleMethodArgumentNotValid(MethodArgumentNotValidException exception) throws Exception {
        List<ErrorBean> errors = exception.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> new ErrorBean(null, fieldError.getField(), fieldError.getDefaultMessage()))
                .toList();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(mapper.writeValueAsString(errors));
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<String> handleHandlerMethodValidation(HandlerMethodValidationException exception) throws Exception {
        List<ErrorBean> errors = exception.getAllValidationResults().stream()
                .flatMap(result -> result.getResolvableErrors().stream())
                .map(error -> new ErrorBean(null, null, error.getDefaultMessage()))
                .toList();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(mapper.writeValueAsString(errors));
    }

    @ExceptionHandler
    public ResponseEntity<String> exceptionHandler(final Exception exception) throws Exception {
        log.error("Unhandled exception occurred: {}", exception.getMessage(), exception);
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "InternalServerError", "An unexpected error occurred");
    }

    private ResponseEntity<String> buildErrorResponse(HttpStatus status, String errorCode, String message) throws Exception {
        List<ErrorBean> errors = List.of(new ErrorBean(errorCode, null, message));
        return ResponseEntity.status(status).body(mapper.writeValueAsString(errors));
    }

    private record ErrorResult(HttpStatus status, String errorCode, String message) {}
}
