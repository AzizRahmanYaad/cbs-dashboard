package com.example.CBS.Dashboard.exception;

public class InvalidRefreshTokenException extends RuntimeException {
    
    public InvalidRefreshTokenException(String message) {
        super(message);
    }
}
