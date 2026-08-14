package com.fuelfinder.backend.dto;

import com.fuelfinder.backend.model.FuelType;

import java.time.LocalDateTime;

// The DTO represents: What we want the client/frontend to see
// while the entity represents: How our data is modeled/persisted internally
// Those don't always need to be the same.

// But our DTO can flatten that relationship:

// FuelPrice
//  ├── price
//  ├── fuelType
//  └── station
//       ├── name
//       ├── address
//       └── zipCode

// into:

// FuelPriceResponse
//  ├── stationName
//  ├── address
//  ├── zipCode
//  ├── price
//  └── fuelType

// That's much easier for React to use later.

public class FuelPriceResponse { // what the BACKEND sends back to the client

    private Long stationId;
    private String stationName;
    private String address;
    private String city;
    private String state;
    private String zipCode;
    private Double price;
    private FuelType fuelType;
    private LocalDateTime lastUpdated;

    public FuelPriceResponse(
            Long stationId,
            String stationName,
            String address,
            String city,
            String state,
            String zipCode,
            Double price,
            FuelType fuelType,
            LocalDateTime lastUpdated) {

        this.stationId = stationId;
        this.stationName = stationName;
        this.address = address;
        this.city = city;
        this.state = state;
        this.zipCode = zipCode;
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

