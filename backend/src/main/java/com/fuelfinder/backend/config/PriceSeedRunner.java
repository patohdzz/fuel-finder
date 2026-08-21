package com.fuelfinder.backend.config;

import com.fuelfinder.backend.service.PriceSeedService;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(
        name = "fuelfinder.price-seed.enabled",
        havingValue = "true"
)
public class PriceSeedRunner implements CommandLineRunner {

    private final PriceSeedService priceSeedService;

    public PriceSeedRunner(PriceSeedService priceSeedService) {
        this.priceSeedService = priceSeedService;
    }

    @Override
    public void run(String... args) {
        // Same crash-proofing as StationImportRunner -- a failed seed must
        // never take the whole app down with it.
        try {
            priceSeedService.seedMissingPrices();
        } catch (Exception e) {
            System.out.println("Price seed failed, continuing startup without it: " + e.getMessage());
        }
    }
}
