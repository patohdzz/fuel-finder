package com.fuelfinder.backend.service;

import com.fuelfinder.backend.dto.FuelPriceRequest;
import com.fuelfinder.backend.dto.FuelPriceResponse;
import com.fuelfinder.backend.exception.StationNotFoundException;
import com.fuelfinder.backend.model.FuelPrice;
import com.fuelfinder.backend.model.FuelType;
import com.fuelfinder.backend.model.Station;
import com.fuelfinder.backend.repository.FuelPriceRepository;
import com.fuelfinder.backend.repository.StationRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;

@ExtendWith(MockitoExtension.class) // This tells JUnit: Use Mockito while running this test class.
class FuelPriceServiceTest {

    @Mock // create fake repository objects.
    private FuelPriceRepository fuelPriceRepository;

    @Mock // create fake repository objects.
    private StationRepository stationRepository;

    @InjectMocks // creates the real FuelPriceService we're testing and gives it our fake repositories.
    private FuelPriceService fuelPriceService; 

    @Test
    void createFuelPrice_createsNewFuelPrice_whenPriceDoesNotExist() {

        // ARRANGE
        Long stationId = 1L;

        FuelPriceRequest request = new FuelPriceRequest();
        request.setPrice(3.15);
        request.setFuelType(FuelType.MIDGRADE);

        Station station = mock(Station.class);

        when(station.getId()).thenReturn(1L);
        when(station.getName()).thenReturn("Shell");
        when(station.getAddress()).thenReturn("100 Main St");
        when(station.getCity()).thenReturn("Arlington");
        when(station.getState()).thenReturn("TX");
        when(station.getZipCode()).thenReturn("76010");

        when(stationRepository.findById(stationId))
                .thenReturn(Optional.of(station));

        when(fuelPriceRepository.findByStation_IdAndFuelType(
                stationId,
                FuelType.MIDGRADE
        )).thenReturn(Optional.empty());

        when(fuelPriceRepository.save(any(FuelPrice.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // ACT
        FuelPriceResponse response =
                fuelPriceService.createFuelPrice(stationId, request);

        // ASSERT
        assertEquals(1L, response.getStationId());
        assertEquals("Shell", response.getStationName());
        assertEquals(3.15, response.getPrice());
        assertEquals(FuelType.MIDGRADE, response.getFuelType());
        assertNotNull(response.getLastUpdated());

        verify(fuelPriceRepository, times(1))
                .save(any(FuelPrice.class));
    }

    @Test
    void createFuelPrice_updatesExistingFuelPrice_whenPriceAlreadyExists() {

        // ARRANGE
        Long stationId = 1L;

        FuelPriceRequest request = new FuelPriceRequest();
        request.setPrice(3.25);
        request.setFuelType(FuelType.MIDGRADE);

        Station station = mock(Station.class);

        when(station.getId()).thenReturn(1L);
        when(station.getName()).thenReturn("Shell");
        when(station.getAddress()).thenReturn("100 Main St");
        when(station.getCity()).thenReturn("Arlington");
        when(station.getState()).thenReturn("TX");
        when(station.getZipCode()).thenReturn("76010");

        FuelPrice existingFuelPrice = new FuelPrice();
        existingFuelPrice.setPrice(3.15);
        existingFuelPrice.setFuelType(FuelType.MIDGRADE);
        existingFuelPrice.setStation(station);

        when(stationRepository.findById(stationId))
                .thenReturn(Optional.of(station));

        when(fuelPriceRepository.findByStation_IdAndFuelType(
                stationId,
                FuelType.MIDGRADE
        )).thenReturn(Optional.of(existingFuelPrice));

        when(fuelPriceRepository.save(any(FuelPrice.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // ACT
        FuelPriceResponse response =
                fuelPriceService.createFuelPrice(stationId, request);

        // ASSERT
        assertEquals(3.25, response.getPrice());
        assertEquals(FuelType.MIDGRADE, response.getFuelType());
        assertEquals("Shell", response.getStationName());
        assertNotNull(response.getLastUpdated());

        assertEquals(3.25, existingFuelPrice.getPrice());
        assertNotNull(existingFuelPrice.getLastUpdated());

        verify(fuelPriceRepository, times(1))
                .save(existingFuelPrice);
    }

    @Test
    void createFuelPrice_throwsException_whenStationDoesNotExist() {

        // ARRANGE
        Long stationId = 999L;

        FuelPriceRequest request = new FuelPriceRequest();
        request.setPrice(2.95);
        request.setFuelType(FuelType.REGULAR);

        when(stationRepository.findById(stationId))
                .thenReturn(Optional.empty());

        // ACT + ASSERT
        StationNotFoundException exception = assertThrows(
                StationNotFoundException.class,
                () -> fuelPriceService.createFuelPrice(stationId, request)
        );

        assertEquals(
                "Station with id 999 was not found",
                exception.getMessage()
        );

        verify(fuelPriceRepository, never())
                .save(any(FuelPrice.class));
    }

    @Test
    void getCheapestFuelPricesByFuelType_returnsAllTiedCheapestPrices() {

        // ARRANGE
        Station shell = mock(Station.class);
        when(shell.getId()).thenReturn(1L);
        when(shell.getName()).thenReturn("Shell");
        when(shell.getAddress()).thenReturn("100 Main St");
        when(shell.getCity()).thenReturn("Arlington");
        when(shell.getState()).thenReturn("TX");
        when(shell.getZipCode()).thenReturn("76010");

        Station quikTrip = mock(Station.class);
        when(quikTrip.getId()).thenReturn(3L);
        when(quikTrip.getName()).thenReturn("QuikTrip");
        when(quikTrip.getAddress()).thenReturn("300 Collins St");
        when(quikTrip.getCity()).thenReturn("Arlington");
        when(quikTrip.getState()).thenReturn("TX");
        when(quikTrip.getZipCode()).thenReturn("76014");

        FuelPrice shellPrice = new FuelPrice();
        shellPrice.setPrice(2.79);
        shellPrice.setFuelType(FuelType.REGULAR);
        shellPrice.setStation(shell);
        shellPrice.setLastUpdated(LocalDateTime.now());

        FuelPrice quikTripPrice = new FuelPrice();
        quikTripPrice.setPrice(2.79);
        quikTripPrice.setFuelType(FuelType.REGULAR);
        quikTripPrice.setStation(quikTrip);
        quikTripPrice.setLastUpdated(LocalDateTime.now());

        when(fuelPriceRepository.findCheapestByFuelType(FuelType.REGULAR))
                .thenReturn(List.of(shellPrice, quikTripPrice));

        // ACT
        List<FuelPriceResponse> responses =
                fuelPriceService.getCheapestFuelPricesByFuelType(
                        FuelType.REGULAR
                );

        // ASSERT
        assertEquals(2, responses.size());

        assertEquals("Shell", responses.get(0).getStationName());
        assertEquals(2.79, responses.get(0).getPrice());

        assertEquals("QuikTrip", responses.get(1).getStationName());
        assertEquals(2.79, responses.get(1).getPrice());

        verify(fuelPriceRepository, times(1))
                .findCheapestByFuelType(FuelType.REGULAR);
    }

    @Test
    void getCheapestFuelPricesByFuelTypeAndZipCode_returnsMatchingCheapestPrices() {

        // ARRANGE
        String zipCode = "76010";

        Station shell = mock(Station.class);
        when(shell.getId()).thenReturn(1L);
        when(shell.getName()).thenReturn("Shell");
        when(shell.getAddress()).thenReturn("100 Main St");
        when(shell.getCity()).thenReturn("Arlington");
        when(shell.getState()).thenReturn("TX");
        when(shell.getZipCode()).thenReturn(zipCode);

        FuelPrice shellPrice = new FuelPrice();
        shellPrice.setPrice(2.79);
        shellPrice.setFuelType(FuelType.REGULAR);
        shellPrice.setStation(shell);
        shellPrice.setLastUpdated(LocalDateTime.now());

        when(fuelPriceRepository.findCheapestByFuelTypeAndZipCode(
                FuelType.REGULAR,
                zipCode
        )).thenReturn(List.of(shellPrice));

        // ACT
        List<FuelPriceResponse> responses =
                fuelPriceService.getCheapestFuelPricesByFuelTypeAndZipCode(
                        FuelType.REGULAR,
                        zipCode
                );

        // ASSERT
        assertEquals(1, responses.size());
        assertEquals("Shell", responses.get(0).getStationName());
        assertEquals("76010", responses.get(0).getZipCode());
        assertEquals(2.79, responses.get(0).getPrice());
        assertEquals(FuelType.REGULAR, responses.get(0).getFuelType());

        verify(fuelPriceRepository, times(1))
                .findCheapestByFuelTypeAndZipCode(
                        FuelType.REGULAR,
                        zipCode
                );
    }
}


// ARRANGE
// ↓
// Prepare the situation

// ACT
// ↓
// Run the method being tested

// ASSERT
// ↓
// Check whether the result is correct