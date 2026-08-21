package com.fuelfinder.backend.dto;

import com.fuelfinder.backend.model.FuelType;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class FuelPriceRequest { // what the CLIENT is allowed to send

    // Upper-bound sanity checks live in FuelPriceService instead of here --
    // whether $10 is a reasonable ceiling depends on whether this station
    // already has a price to compare against, which this DTO doesn't know.
    @NotNull(message = "Price is required")
    @Positive(message = "Price must be greater than 0")
    private Double price;

    @NotNull(message = "Fuel type is required")
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