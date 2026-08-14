package com.fuelfinder.backend.controller;

// import com.fuelfinder.backend.model.FuelPrice; NO MORE USE
import com.fuelfinder.backend.service.FuelPriceService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import com.fuelfinder.backend.model.FuelType;
import org.springframework.web.bind.annotation.RequestParam;

import com.fuelfinder.backend.dto.FuelPriceRequest;
import com.fuelfinder.backend.dto.FuelPriceResponse;

import jakarta.validation.Valid;

@RestController // handles API requests
public class FuelPriceController {

    private final FuelPriceService fuelPriceService;

    public FuelPriceController(FuelPriceService fuelPriceService) {
        this.fuelPriceService = fuelPriceService;
    }

    @GetMapping("/api/fuel-prices")
    public List<FuelPriceResponse> getFuelPrices(@RequestParam(required = false) FuelType fuelType) {
        // if they gave us a fuelType to look for, return the helper method, else return all of them
        if (fuelType != null) {
            return fuelPriceService.getFuelPricesByFuelType(fuelType);
        }

        return fuelPriceService.getAllFuelPrices();
    }

    // Spring applies the validation annotations inside FuelPriceRequest before calling your service. 
    // If validation fails, Spring MVC raises a validation exception and returns 400 Bad Request by default.
    @PostMapping("/api/stations/{stationId}/prices")
    public FuelPriceResponse createFuelPrice(@PathVariable Long stationId, @Valid @RequestBody FuelPriceRequest request) {
        return fuelPriceService.createFuelPrice(stationId, request);
    }

    @GetMapping("/api/stations/{stationId}/prices")
    public List<FuelPriceResponse> getFuelPricesByStation(@PathVariable Long stationId) {
        return fuelPriceService.getFuelPricesByStation(stationId);
    }

    @GetMapping("/api/fuel-prices/cheapest")
    public List<FuelPriceResponse> getCheapestFuelPrices(@RequestParam FuelType fuelType, @RequestParam(required = false) String zipCode) {
        if (zipCode != null) {
            return fuelPriceService.getCheapestFuelPricesByFuelTypeAndZipCode(fuelType, zipCode);
        }
        return fuelPriceService.getCheapestFuelPricesByFuelType(fuelType);
    }
}

// The complete POST flow will now be: 
// POST /api/stations/1/prices

// JSON:
// {
//   "price": 2.89,
//   "fuelType": "REGULAR"
// }
//         ↓
// FuelPriceController

// @PathVariable
// stationId = 1

// @RequestBody
// JSON → FuelPrice object
//         ↓
// FuelPriceService

// find station id 1
//         ↓
// fuelPrice.setStation(station)
//         ↓
// set current timestamp
//         ↓
// FuelPriceRepository.save()
//         ↓
// Hibernate
//         ↓
// MySQL

// fuel_prices:
// id | price | fuel_type | station_id | last_updated
// 1  | 2.89  | REGULAR   | 1          | ...