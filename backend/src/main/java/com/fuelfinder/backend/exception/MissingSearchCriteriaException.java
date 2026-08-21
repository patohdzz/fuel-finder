package com.fuelfinder.backend.exception;

public class MissingSearchCriteriaException extends RuntimeException {

    public MissingSearchCriteriaException() {
        super("Provide a zipCode, a city, or both to search.");
    }
}
