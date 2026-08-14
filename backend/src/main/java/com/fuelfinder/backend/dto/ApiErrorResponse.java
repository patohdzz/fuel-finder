package com.fuelfinder.backend.dto;

import java.time.LocalDateTime;

public class ApiErrorResponse {
    // We don't want random exception objects being exposed directly to the client. 
    // Instead, we decide exactly what our API error contract looks like.

    private int status;
    private String error;
    private String message;
    private LocalDateTime timestamp;

    public ApiErrorResponse(
            int status,
            String error,
            String message,
            LocalDateTime timestamp) {

        this.status = status;
        this.error = error;
        this.message = message;
        this.timestamp = timestamp;
    }

    public int getStatus() {
        return status;
    }

    public String getError() {
        return error;
    }

    public String getMessage() {
        return message;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}