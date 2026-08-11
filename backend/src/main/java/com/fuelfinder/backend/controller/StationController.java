package com.fuelfinder.backend.controller;

import com.fuelfinder.backend.model.Station;
import com.fuelfinder.backend.service.StationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@RestController // means this class handles API requests
public class StationController {

    private final StationService stationService; // means the controller needs the service layer

    // dependency injection again, spring is giving the controller a stationService
    public StationController(StationService stationService) {
        this.stationService = stationService;
    }

    // which gets every station
    @GetMapping("/api/stations")
    public List<Station> getAllStations() {
        return stationService.getAllStations();
    }

    // which creates a station
    @PostMapping("/api/stations")
    public Station createStation(@RequestBody Station station) {
        return stationService.createStation(station);
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