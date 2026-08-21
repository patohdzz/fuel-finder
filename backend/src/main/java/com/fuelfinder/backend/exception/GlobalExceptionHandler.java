package com.fuelfinder.backend.exception;

import com.fuelfinder.backend.dto.ApiErrorResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

import com.fuelfinder.backend.exception.StationNotFoundException;
import org.springframework.http.converter.HttpMessageNotReadableException;

@RestControllerAdvice // “This class contains error-handling rules that apply to all my REST controllers.”
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class) // “Run this method whenever a MethodArgumentNotValidException happens.”
    public ResponseEntity<ApiErrorResponse> handleValidationException(
            MethodArgumentNotValidException exception) {

        String message = exception
                .getBindingResult()
                .getFieldErrors()
                .get(0)
                .getDefaultMessage();

        ApiErrorResponse errorResponse = new ApiErrorResponse(
                400,
                "Bad Request",
                message,
                LocalDateTime.now()
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorResponse);
    }

    @ExceptionHandler(StationNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleStationNotFoundException(
            StationNotFoundException exception) {

        ApiErrorResponse errorResponse = new ApiErrorResponse(
                404,
                "Not Found",
                exception.getMessage(),
                LocalDateTime.now()
        );

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(errorResponse);
    }

    @ExceptionHandler(ImplausiblePriceException.class)
    public ResponseEntity<ApiErrorResponse> handleImplausiblePriceException(
            ImplausiblePriceException exception) {

        ApiErrorResponse errorResponse = new ApiErrorResponse(
                400,
                "Bad Request",
                exception.getMessage(),
                LocalDateTime.now()
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorResponse);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidRequestBody(
            HttpMessageNotReadableException exception) {

        ApiErrorResponse errorResponse = new ApiErrorResponse(
                400,
                "Bad Request",
                "Invalid request body. Check that the fuel type is valid.",
                LocalDateTime.now()
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorResponse);
    }
}

// Now your global handler understands three categories:

// MethodArgumentNotValidException
// → validation rule failed
// → 400


// HttpMessageNotReadableException
// → JSON could not be converted correctly
// → 400


// StationNotFoundException
// → station does not exist
// → 404

// This is useful because @Valid and JSON conversion happen at different stages.