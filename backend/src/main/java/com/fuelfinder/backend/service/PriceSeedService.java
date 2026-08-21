package com.fuelfinder.backend.service;

import com.fuelfinder.backend.model.FuelPrice;
import com.fuelfinder.backend.model.FuelType;
import com.fuelfinder.backend.model.Station;
import com.fuelfinder.backend.repository.FuelPriceRepository;
import com.fuelfinder.backend.repository.StationRepository;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class PriceSeedService {

    // U.S. EIA "Weekly Retail Gasoline and Diesel Prices," Gulf Coast (PADD 3)
    // region -- the only region/state-level breakdown EIA publishes for these
    // grades. Week ending 2026-08-17, released 2026-08-18.
    // https://www.eia.gov/petroleum/gasdiesel/
    // Midgrade/Premium aren't published at this regional granularity, so
    // those fuel types are intentionally left unseeded rather than guessed.
    private static final Map<FuelType, Double> SEED_PRICES = Map.of(
            FuelType.REGULAR, 3.622,
            FuelType.DIESEL, 5.237
    );

    private final StationRepository stationRepository;
    private final FuelPriceRepository fuelPriceRepository;

    public PriceSeedService(StationRepository stationRepository, FuelPriceRepository fuelPriceRepository) {
        this.stationRepository = stationRepository;
        this.fuelPriceRepository = fuelPriceRepository;
    }

    // One-time bootstrap: gives every station missing a Regular or Diesel
    // price a real, EIA-sourced regional starting value instead of staying
    // blank. Never overwrites a price that's already there -- only fills in
    // what's genuinely missing.
    public void seedMissingPrices() {
        List<Station> stations = stationRepository.findAll();
        int seeded = 0;
        int alreadyPriced = 0;

        for (Station station : stations) {
            for (Map.Entry<FuelType, Double> entry : SEED_PRICES.entrySet()) {
                FuelType fuelType = entry.getKey();
                Double price = entry.getValue();

                boolean alreadyExists = fuelPriceRepository
                        .findByStation_IdAndFuelType(station.getId(), fuelType)
                        .isPresent();

                if (alreadyExists) {
                    alreadyPriced++;
                    continue;
                }

                FuelPrice fuelPrice = new FuelPrice();
                fuelPrice.setStation(station);
                fuelPrice.setFuelType(fuelType);
                fuelPrice.setPrice(price);
                fuelPrice.setLastUpdated(LocalDateTime.now());

                fuelPriceRepository.save(fuelPrice);
                seeded++;
            }
        }

        System.out.println(
                "Price seed complete - seeded: " + seeded
                + ", already priced: " + alreadyPriced
        );
    }
}
