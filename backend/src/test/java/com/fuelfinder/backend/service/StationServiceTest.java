package com.fuelfinder.backend.service;

import com.fuelfinder.backend.dto.StationSearchResponse;
import com.fuelfinder.backend.exception.MissingSearchCriteriaException;
import com.fuelfinder.backend.model.FuelType;
import com.fuelfinder.backend.repository.StationRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StationServiceTest {

    @Mock
    private StationRepository stationRepository;

    @InjectMocks
    private StationService stationService;

    @Test
    void searchStations_throwsException_whenNeitherZipCodeNorCityGiven() {
        assertThrows(
                MissingSearchCriteriaException.class,
                () -> stationService.searchStations(null, null, FuelType.REGULAR)
        );

        assertThrows(
                MissingSearchCriteriaException.class,
                () -> stationService.searchStations("  ", "", FuelType.REGULAR)
        );

        verify(stationRepository, never())
                .searchStationsByZipCodeAndCityAndFuelType(any(), any(), any());
    }

    @Test
    void searchStations_searchesByZipCodeOnly_whenCityBlank() {
        when(stationRepository.searchStationsByZipCodeAndCityAndFuelType("76010", null, FuelType.REGULAR))
                .thenReturn(List.of(mock(StationSearchResponse.class)));

        List<StationSearchResponse> results =
                stationService.searchStations("76010", "  ", FuelType.REGULAR);

        assertEquals(1, results.size());
        verify(stationRepository).searchStationsByZipCodeAndCityAndFuelType(
                eq("76010"), isNull(), eq(FuelType.REGULAR)
        );
    }

    @Test
    void searchStations_searchesByCityOnly_whenZipCodeBlank() {
        when(stationRepository.searchStationsByZipCodeAndCityAndFuelType(null, "Arlington", FuelType.DIESEL))
                .thenReturn(List.of(mock(StationSearchResponse.class)));

        List<StationSearchResponse> results =
                stationService.searchStations(null, "Arlington", FuelType.DIESEL);

        assertEquals(1, results.size());
        verify(stationRepository).searchStationsByZipCodeAndCityAndFuelType(
                isNull(), eq("Arlington"), eq(FuelType.DIESEL)
        );
    }

    @Test
    void searchStations_searchesByBoth_whenBothGiven() {
        when(stationRepository.searchStationsByZipCodeAndCityAndFuelType("76010", "Arlington", FuelType.PREMIUM))
                .thenReturn(List.of());

        stationService.searchStations("76010", "Arlington", FuelType.PREMIUM);

        verify(stationRepository).searchStationsByZipCodeAndCityAndFuelType(
                eq("76010"), eq("Arlington"), eq(FuelType.PREMIUM)
        );
    }
}
