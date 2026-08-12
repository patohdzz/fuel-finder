package com.fuelfinder.backend.controller;

import com.fuelfinder.backend.model.FuelPrice;
import com.fuelfinder.backend.service.FuelPriceService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController // handles API requests
public class FuelPriceController {

    private final FuelPriceService fuelPriceService;

    public FuelPriceController(FuelPriceService fuelPriceService) {
        this.fuelPriceService = fuelPriceService;
    }

    @GetMapping("/api/fuel-prices")
    public List<FuelPrice> getAllFuelPrices() {
        return fuelPriceService.getAllFuelPrices();
    }

    @PostMapping("/api/stations/{stationId}/prices")
    public FuelPrice createFuelPrice(
            @PathVariable Long stationId,
            @RequestBody FuelPrice fuelPrice) {

        return fuelPriceService.createFuelPrice(stationId, fuelPrice);
    }

    @GetMapping("/api/stations/{stationId}/prices")
    public List<FuelPrice> getFuelPricesByStation(
            @PathVariable Long stationId) {

        return fuelPriceService.getFuelPricesByStation(stationId);
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