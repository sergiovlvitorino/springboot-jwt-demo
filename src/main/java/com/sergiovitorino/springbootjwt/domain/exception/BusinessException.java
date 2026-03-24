package com.sergiovitorino.springbootjwt.domain.exception;

public sealed class BusinessException extends RuntimeException
        permits ResourceNotFoundException, EmailAlreadyExistsException {
    public BusinessException(String message) {
        super(message);
    }
}
