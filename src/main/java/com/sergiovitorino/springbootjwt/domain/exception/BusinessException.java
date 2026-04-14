package com.sergiovitorino.springbootjwt.domain.exception;

public abstract sealed class BusinessException extends RuntimeException
        permits ResourceNotFoundException, EmailAlreadyExistsException {
    protected BusinessException(String message) {
        super(message);
    }
}
