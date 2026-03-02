package com.sergiovitorino.springbootjwt.domain.exception;

public class EmailAlreadyExistsException extends BusinessException {
    public EmailAlreadyExistsException(String message) {
        super(message);
    }
}
