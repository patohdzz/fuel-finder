package com.fuelfinder.backend.service;

import com.fuelfinder.backend.model.Station;
import com.fuelfinder.backend.repository.StationRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
// this class will contain business logic
public class StationService {

    private final StationRepository stationRepository;

    // DEPENDENCY INJECTION
    // this means that spring automatically gives StationService a stationRepository object
    public StationService(StationRepository stationRepository) {
        this.stationRepository = stationRepository;
    }

    public List<Station> getAllStations() {
        return stationRepository.findAll();
    }

    // So instead of manually inserting stations in MySQL Workbench, 
    // we’ll be able to send station data to the backend, and the backend will save it.
    public Station createStation(Station station) {
        return stationRepository.save(station);
    }
}