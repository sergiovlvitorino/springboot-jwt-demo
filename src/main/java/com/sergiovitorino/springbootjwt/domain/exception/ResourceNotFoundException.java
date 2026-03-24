package com.sergiovitorino.springbootjwt.domain.exception;

public final class ResourceNotFoundException extends BusinessException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
