package com.fuelfinder.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;


@Entity // Tells JPA that this Java class represents database data.
@Table(name = "stations") // Connects the class to the stations table.

public class Station { 
    // this class represents a station inside the application
    // it also maps to the stations table

    @Id // marks the primary key
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Tells MySQL to automatically generate the ID.
    private Long id;

    private String name;
    private String address;
    private String city;
    private String state;

    @Column(name = "zip_code") // Connects the Java variable zipCode to the SQL column zip_code.
    private String zipCode;

    private Double latitude;
    private Double longitude;

    public Station() {
    }

    public Station(String name, String address, String city, String state, String zipCode, Double latitude, Double longitude) {
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

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }
}