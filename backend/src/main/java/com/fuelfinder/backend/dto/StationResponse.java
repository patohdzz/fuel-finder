package com.fuelfinder.backend.dto;

public class StationResponse {
    // StationRequest  = what client is allowed to send
    // StationResponse = what backend chooses to return
    // Station         = database/persistence model

    private Long id;
    private String name;
    private String address;
    private String city;
    private String state;
    private String zipCode;
    private Double latitude;
    private Double longitude;

    public StationResponse(
            Long id,
            String name,
            String address,
            String city,
            String state,
            String zipCode,
            Double latitude,
            Double longitude) {

        this.id = id;
        this.name = name;
        this.address = address;
        this.city = city;
        this.state = state;
        this.zipCode = zipCode;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
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
}