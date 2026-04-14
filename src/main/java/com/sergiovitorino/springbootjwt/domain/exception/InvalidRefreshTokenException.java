package com.sergiovitorino.springbootjwt.domain.exception;

public final class InvalidRefreshTokenException extends BusinessException {
    public InvalidRefreshTokenException(String message) {
        super(message);
    }
}
