package com.sergiovitorino.springbootjwt.domain.exception;

public final class EmailAlreadyExistsException extends BusinessException {
    public EmailAlreadyExistsException(String message) {
        super(message);
    }
}
