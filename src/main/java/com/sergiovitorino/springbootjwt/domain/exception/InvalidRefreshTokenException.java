package com.sergiovitorino.springbootjwt.domain.exception;

public class InvalidRefreshTokenException extends BusinessException {
    public InvalidRefreshTokenException(String message) {
        super(message);
    }
}
