package com.fuelfinder.backend.service;

import com.fuelfinder.backend.model.FuelPrice;
import com.fuelfinder.backend.repository.FuelPriceRepository;
import org.springframework.stereotype.Service;

import java.util.List;

import com.fuelfinder.backend.model.Station;
import com.fuelfinder.backend.repository.StationRepository;
import java.time.LocalDateTime;

@Service
public class FuelPriceService {

    private final FuelPriceRepository fuelPriceRepository;
    private final StationRepository stationRepository;

    public FuelPriceService(FuelPriceRepository fuelPriceRepository, StationRepository stationRepository) {
        this.fuelPriceRepository = fuelPriceRepository;
        this.stationRepository = stationRepository;
    }

    public List<FuelPrice> getAllFuelPrices() { // Ask the repository for every fuel price in the database.
        return fuelPriceRepository.findAll();
    }

    public FuelPrice createFuelPrice(Long stationId, FuelPrice fuelPrice) { // Take a FuelPrice object and save it to MySQL.
        // For: POST /api/stations/1/prices
        // we are effectively looking for:
            // SELECT *
            // FROM stations
            // WHERE id = 1;
        Station station = stationRepository.findById(stationId).orElseThrow(() -> new RuntimeException("Station not found"));
        // If the station doesn't exist, stop and throw an error instead of trying to create a price for a nonexistent station. 
        // That protects our foreign-key relationship.

        fuelPrice.setStation(station); // connects the Java objects
        fuelPrice.setLastUpdated(LocalDateTime.now());

        return fuelPriceRepository.save(fuelPrice);
    }

    public List<FuelPrice> getFuelPricesByStation(Long stationId) {
        return fuelPriceRepository.findByStation_Id(stationId);
    }
}