package com.fuelfinder.backend.exception;

public class StationNotFoundException extends RuntimeException {

    public StationNotFoundException(Long stationId) {
        super("Station with id " + stationId + " was not found");
    }
}