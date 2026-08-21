package com.fuelfinder.backend.service;

import com.fuelfinder.backend.model.FuelPrice;
import com.fuelfinder.backend.model.FuelType;
import com.fuelfinder.backend.repository.FuelPriceRepository;
import org.springframework.stereotype.Service;

import java.util.List;

import com.fuelfinder.backend.model.Station;
import com.fuelfinder.backend.repository.StationRepository;
import java.time.LocalDateTime;

import com.fuelfinder.backend.dto.FuelPriceRequest;
import com.fuelfinder.backend.dto.FuelPriceResponse;

import com.fuelfinder.backend.exception.StationNotFoundException;
import com.fuelfinder.backend.exception.ImplausiblePriceException;

@Service // this class will contain business logic
public class FuelPriceService {

    private static final double MAX_PRICE_CHANGE = 1.00; // guards against typos like $1.00 instead of $2.80
    private static final double FIRST_REPORT_MAX_PRICE = 10.0; // no existing price to compare a first report against

    private final FuelPriceRepository fuelPriceRepository;
    private final StationRepository stationRepository;

    public FuelPriceService(FuelPriceRepository fuelPriceRepository, StationRepository stationRepository) {
        this.fuelPriceRepository = fuelPriceRepository;
        this.stationRepository = stationRepository;
    }

    public List<FuelPriceResponse> getAllFuelPrices() { // Ask the repository for every fuel price in the database.
        return fuelPriceRepository.findAll().stream().map(this::toResponse).toList();
    }

    public FuelPriceResponse createFuelPrice(Long stationId, FuelPriceRequest request) {
        // For: POST /api/stations/1/prices
        // we are effectively looking for:
            // SELECT *
            // FROM stations
            // WHERE id = 1;
        Station station = stationRepository.findById(stationId).orElseThrow(() -> new StationNotFoundException(stationId));
        // If the station doesn't exist, stop and throw an error instead of trying to create a price for a nonexistent station. 
        // That protects our foreign-key relationship.

        // look for if a price exists already, if so update it, if not create a new one
        FuelPrice fuelPrice = fuelPriceRepository.findByStation_IdAndFuelType(stationId, request.getFuelType()).orElse(new FuelPrice());

        if (fuelPrice.getPrice() == null) {
            // No existing price to compare against -- fall back to a flat ceiling.
            if (request.getPrice() > FIRST_REPORT_MAX_PRICE) {
                throw new ImplausiblePriceException(String.format(
                        "Price cannot be greater than $%.2f for a station's first reported price.",
                        FIRST_REPORT_MAX_PRICE));
            }
        } else if (Math.abs(request.getPrice() - fuelPrice.getPrice()) > MAX_PRICE_CHANGE) {
            throw new ImplausiblePriceException(fuelPrice.getPrice(), request.getPrice());
        }

        // We're creating the actual database entity ourselves.
        // Then we copy only the fields the client is allowed to control:
        fuelPrice.setPrice(request.getPrice());
        fuelPrice.setFuelType(request.getFuelType());
        fuelPrice.setStation(station); // connects the Java objects
        fuelPrice.setLastUpdated(LocalDateTime.now());

        // results in either and update or an insert
        FuelPrice savedFuelPrice = fuelPriceRepository.save(fuelPrice); // causes Hibernate/JPA to update the row in MySQL.

        return toResponse(savedFuelPrice);
    }

    public List<FuelPriceResponse> getFuelPricesByStation(Long stationId) {
        return fuelPriceRepository.findByStation_Id(stationId).stream().map(this::toResponse).toList();
    }

    
    public List<FuelPriceResponse> getFuelPricesByFuelType(FuelType fuelType) {
        return fuelPriceRepository.findByFuelType(fuelType).stream().map(this::toResponse).toList();
        // “Give me all fuel-price records where the fuel type is REGULAR.”
    }

    // modify to return the DTO
    public List<FuelPriceResponse> getCheapestFuelPricesByFuelType(FuelType fuelType) {
        return fuelPriceRepository.findCheapestByFuelType(fuelType).stream().map(this::toResponse).toList();
        // A stream lets us process each element in the list.
        // map means: Take each FuelPrice and run it through our toResponse() method.

        // FuelPrice #1
        //     ↓
        // toResponse()
        //     ↓
        // FuelPriceResponse #1

        // FuelPrice #2
        //     ↓
        // toResponse()
        //     ↓
        // FuelPriceResponse #2
    }    

    public List<FuelPriceResponse> getCheapestFuelPricesByFuelTypeAndZipCode(FuelType fuelType, String zipCode) {
        return fuelPriceRepository.findCheapestByFuelTypeAndZipCode(fuelType, zipCode).stream().map(this::toResponse).toList();
    }

    public List<FuelPriceResponse> getFuelPricesByFuelTypeAndZipCode(FuelType fuelType, String zipCode) {

        return fuelPriceRepository.findByFuelTypeAndStation_ZipCodeOrderByPriceAsc(fuelType, zipCode).stream().map(this::toResponse).toList();
    }

    // We aren't making this a public service operation that the controller calls directly.
    // It's just a helper method used internally by FuelPriceService.
    private FuelPriceResponse toResponse(FuelPrice fuelPrice) {
        Station station = fuelPrice.getStation();

        // FuelPriceResponse DTO represents the API response
        return new FuelPriceResponse(
                station.getId(),
                station.getName(),
                station.getAddress(),
                station.getCity(),
                station.getState(),
                station.getZipCode(),
                fuelPrice.getPrice(),
                fuelPrice.getFuelType(),
                fuelPrice.getLastUpdated()
        );
    }


}