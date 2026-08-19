package com.fuelfinder.backend.exception;

public class OverpassApiException extends RuntimeException {

    public OverpassApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
