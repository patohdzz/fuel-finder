package com.fuelfinder.backend.dto;

import com.fuelfinder.backend.model.FuelType;

public class FuelPriceRequest { // what the CLIENT is allowed to send

    private Double price;
    private FuelType fuelType;

    public FuelPriceRequest() {
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
}