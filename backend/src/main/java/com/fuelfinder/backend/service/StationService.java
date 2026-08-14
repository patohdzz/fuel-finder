package com.fuelfinder.backend.service;

import com.fuelfinder.backend.model.Station;
import com.fuelfinder.backend.repository.StationRepository;
import org.springframework.stereotype.Service;

import java.util.List;

import javax.swing.Spring;

@Service  // this class will contain business logic

public class StationService {

    private final StationRepository stationRepository;

    // DEPENDENCY INJECTION
    // this means that spring automatically gives StationService a stationRepository object
    public StationService(StationRepository stationRepository) {
        this.stationRepository = stationRepository;
        // You do not manually write: new StationRepository();
        // Spring creates and manages the object for you.
    }

    public List<Station> getAllStations() {
        return stationRepository.findAll();
    }

    // So instead of manually inserting stations in MySQL Workbench, 
    // we’ll be able to send station data to the backend, and the backend will save it.
    public Station createStation(Station station) {
        return stationRepository.save(station);
    }

    // Later, the service can contain logic such as:
        // Sorting stations by price
        // Filtering by fuel type
        // Calculating savings
        // Validating user input

    public List<Station> getStationsByZipCode(String zipCode) {
        return stationRepository.findByZipCode(zipCode);
    }  

}