package com.fuelfinder.backend.dto;

import com.fuelfinder.backend.model.FuelType;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class FuelPriceRequest { // what the CLIENT is allowed to send

    @NotNull(message = "Price is required")
    @Positive(message = "Price must be greater than 0")
    @DecimalMax(value = "10.0", message = "Price cannot be greater than $10")
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