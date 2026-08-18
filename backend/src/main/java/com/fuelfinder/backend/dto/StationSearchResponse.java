package com.fuelfinder.backend.dto;

import com.fuelfinder.backend.model.FuelType;

import java.time.LocalDateTime;

public class StationSearchResponse {

    private Long stationId;
    private String stationName;
    private String address;
    private String city;
    private String state;
    private String zipCode;

    private Double latitude;
    private Double longitude;

    private Double price;
    private FuelType fuelType;
    private LocalDateTime lastUpdated;

    public StationSearchResponse(
            Long stationId,
            String stationName,
            String address,
            String city,
            String state,
            String zipCode,
            Double latitude,
            Double longitude,
            Double price,
            FuelType fuelType,
            LocalDateTime lastUpdated) {

        this.stationId = stationId;
        this.stationName = stationName;
        this.address = address;
        this.city = city;
        this.state = state;
        this.zipCode = zipCode;
        this.latitude = latitude;
        this.longitude = longitude;
        this.price = price;
        this.fuelType = fuelType;
        this.lastUpdated = lastUpdated;
    }

    public Long getStationId() {
        return stationId;
    }

    public String getStationName() {
        return stationName;
    }

    public String getAddress() {
        return address;
    }

    public String getCity() {
        return city;
    }

    public String getState() {
        return state;
    }

    public String getZipCode() {
        return zipCode;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public Double getPrice() {
        return price;
    }

    public FuelType getFuelType() {
        return fuelType;
    }

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }
}