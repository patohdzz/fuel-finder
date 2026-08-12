package com.fuelfinder.backend.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.criteria.CriteriaBuilder.In;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Column;

import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import java.time.LocalDateTime;

@Entity
@Table(name = "fuel_prices") // new table, from stations pov its a one to many relationship/from fuel_prices its a many to one
public class FuelPrice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double price;

    @Enumerated(EnumType.STRING) // tells Hibernate: Store the name of the enum value as text in MySQL.
    @Column(name = "fuel_type")  // connect java fuelType to SQL fuel_type
    private FuelType fuelType;

    @ManyToOne // many fuel_price objects that can belong to one station
    @JoinColumn(name = "station_id") // tells Hibernate: In the fuel_prices table, create/use a column called station_id that identifies which station this price belongs to.
    private Station station;

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;

    public FuelPrice() {
    }

    public Long getId() {
        return id;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }


    public FuelType getFuelType() {
        return fuelType;
    }

    public void setFuelType(FuelType fuelType) {
        this.fuelType = fuelType;
    }

    public Station getStation() {
        return station;
    }

    public void setStation(Station station) {
        this.station = station;
    }

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
}
}