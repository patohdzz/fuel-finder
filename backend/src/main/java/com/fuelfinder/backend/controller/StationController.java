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

    // when a GET request is sent, run this
    @GetMapping("/api/stations")
    public List<Station> getAllStations() {
        return stationService.getAllStations();
    }

    // Take the JSON sent by the user and turn it into a Java Station object.
    @PostMapping("/api/stations")
    public Station createStation(@RequestBody Station station) {
        return stationService.createStation(station);
    }
}