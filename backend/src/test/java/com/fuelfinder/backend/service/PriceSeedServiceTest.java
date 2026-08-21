package com.fuelfinder.backend.service;

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

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PriceSeedServiceTest {

    @Mock
    private StationRepository stationRepository;

    @Mock
    private FuelPriceRepository fuelPriceRepository;

    @InjectMocks
    private PriceSeedService priceSeedService;

    @Test
    void seedsRegularAndDiesel_forAStationWithNoPricesAtAll() {
        Station station = mock(Station.class);
        when(station.getId()).thenReturn(1L);

        when(stationRepository.findAll()).thenReturn(List.of(station));
        when(fuelPriceRepository.findByStation_IdAndFuelType(1L, FuelType.REGULAR))
                .thenReturn(Optional.empty());
        when(fuelPriceRepository.findByStation_IdAndFuelType(1L, FuelType.DIESEL))
                .thenReturn(Optional.empty());

        priceSeedService.seedMissingPrices();

        verify(fuelPriceRepository, times(2)).save(any(FuelPrice.class));
    }

    @Test
    void doesNotOverwrite_aPriceThatAlreadyExists() {
        Station station = mock(Station.class);
        when(station.getId()).thenReturn(1L);

        FuelPrice existingRegular = new FuelPrice();
        existingRegular.setPrice(2.79);

        when(stationRepository.findAll()).thenReturn(List.of(station));
        when(fuelPriceRepository.findByStation_IdAndFuelType(1L, FuelType.REGULAR))
                .thenReturn(Optional.of(existingRegular));
        when(fuelPriceRepository.findByStation_IdAndFuelType(1L, FuelType.DIESEL))
                .thenReturn(Optional.empty());

        priceSeedService.seedMissingPrices();

        // Only diesel was missing -- regular already had a real price and must be left alone.
        verify(fuelPriceRepository, times(1)).save(any(FuelPrice.class));
    }

    @Test
    void doesNothing_whenEveryStationAlreadyHasBothPrices() {
        Station station = mock(Station.class);
        when(station.getId()).thenReturn(1L);

        FuelPrice existingRegular = new FuelPrice();
        existingRegular.setPrice(2.79);
        FuelPrice existingDiesel = new FuelPrice();
        existingDiesel.setPrice(3.49);

        when(stationRepository.findAll()).thenReturn(List.of(station));
        when(fuelPriceRepository.findByStation_IdAndFuelType(1L, FuelType.REGULAR))
                .thenReturn(Optional.of(existingRegular));
        when(fuelPriceRepository.findByStation_IdAndFuelType(1L, FuelType.DIESEL))
                .thenReturn(Optional.of(existingDiesel));

        priceSeedService.seedMissingPrices();

        verify(fuelPriceRepository, never()).save(any(FuelPrice.class));
    }
}
