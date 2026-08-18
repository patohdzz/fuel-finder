package com.fuelfinder.backend.service;

import com.fuelfinder.backend.dto.StationRequest;
import com.fuelfinder.backend.dto.StationResponse;
import com.fuelfinder.backend.model.Station;
import com.fuelfinder.backend.repository.StationRepository;

import org.springframework.stereotype.Service;

import java.util.List;

import com.fuelfinder.backend.dto.StationSearchResponse;
import com.fuelfinder.backend.model.FuelType;


@Service   // this class will contain business logic
public class StationService {

    private final StationRepository stationRepository;

    // DEPENDENCY INJECTION
    // this means that spring automatically gives StationService a stationRepository object
    public StationService(StationRepository stationRepository) {
        this.stationRepository = stationRepository;
        // You do not manually write: new StationRepository();
        // Spring creates and manages the object for you.
    }

    public List<StationResponse> getAllStations() {
        return stationRepository.findAll().stream().map(this::toResponse).toList();
    }

    public List<StationResponse> getStationsByZipCode(String zipCode) {
        return stationRepository.findByZipCode(zipCode).stream().map(this::toResponse).toList();
    }

    // So instead of manually inserting stations in MySQL Workbench, 
    // we’ll be able to send station data to the backend, and the backend will save it.
    public StationResponse createStation(StationRequest request) {

        Station station = new Station();

        station.setName(request.getName());
        station.setAddress(request.getAddress());
        station.setCity(request.getCity());
        station.setState(request.getState());
        station.setZipCode(request.getZipCode());
        station.setLatitude(request.getLatitude());
        station.setLongitude(request.getLongitude());

        Station savedStation = stationRepository.save(station);

        return toResponse(savedStation);
    }

    private StationResponse toResponse(Station station) {
        return new StationResponse(
                station.getId(),
                station.getName(),
                station.getAddress(),
                station.getCity(),
                station.getState(),
                station.getZipCode(),
                station.getLatitude(),
                station.getLongitude()
        );
    }

    public List<StationSearchResponse> searchStations(String zipCode, FuelType fuelType) {

        return stationRepository.searchStationsByZipCodeAndFuelType(zipCode, fuelType);
    }
}
