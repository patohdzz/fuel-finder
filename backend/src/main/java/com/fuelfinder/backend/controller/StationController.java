package com.fuelfinder.backend.controller;

import com.fuelfinder.backend.dto.StationRequest;
import com.fuelfinder.backend.dto.StationResponse;
import com.fuelfinder.backend.service.StationService;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.*;

import java.util.List;

import com.fuelfinder.backend.dto.StationSearchResponse;
import com.fuelfinder.backend.model.FuelType;


@RestController  // means this class handles API requests
public class StationController {

    private final StationService stationService;  // means the controller needs the service layer

    // dependency injection again, spring is giving the controller a stationService
    public StationController(StationService stationService) {
        this.stationService = stationService;
    }

    // which gets every station
    @GetMapping("/api/stations")
    public List<StationResponse> getStations(@RequestParam(required = false) String zipCode) {

        if (zipCode != null) {
            return stationService.getStationsByZipCode(zipCode);
        }

        return stationService.getAllStations();
    }

    // which creates a station
    @PostMapping("/api/stations")
    public StationResponse createStation(@Valid @RequestBody StationRequest request) {

        return stationService.createStation(request);
    }

    @GetMapping("/api/stations/search")
    public List<StationSearchResponse> searchStations(
            @RequestParam String zipCode,
            @RequestParam FuelType fuelType) {

        return stationService.searchStations(zipCode, fuelType);
    }
}


// When the browser sends:

// GET http://localhost:8080/api/stations

// this happens:

// Browser
//    ↓
// StationController.getAllStations()
//    ↓
// StationService.getAllStations()
//    ↓
// StationRepository.findAll()
//    ↓
// MySQL runs SELECT
//    ↓
// Station objects are returned
//    ↓
// Spring converts them to JSON

// The response might look like:

// [
//   {
//     "id": 1,
//     "name": "Shell",
//     "address": "123 Main Street",
//     "city": "Arlington",
//     "state": "TX",
//     "zipCode": "76010"
//   }
// ]