package com.fuelfinder.backend.config;

import com.fuelfinder.backend.service.OsmStationImporter;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(
        name = "fuelfinder.osm.import-enabled",
        havingValue = "true"
)
public class StationImportRunner implements CommandLineRunner {

    private final OsmStationImporter osmStationImporter;

    public StationImportRunner(
            OsmStationImporter osmStationImporter) {

        this.osmStationImporter = osmStationImporter;
    }

    @Override
    public void run(String... args) {
        // A failed import must never take the whole app down with it --
        // if this rethrew, a CommandLineRunner failure fails the entire
        // Spring ApplicationContext, meaning the web server never starts
        // and the live site goes down. Caught and logged instead: the app
        // starts normally either way, just without fresh station data.
        try {
            osmStationImporter.importDfwStations();
        } catch (Exception e) {
            System.out.println("OSM import failed, continuing startup without it: " + e.getMessage());
        }
    }
}

// Normal FuelFinder startup:

// .\mvnw.cmd spring-boot:run

// No import happens.

// When you intentionally want to refresh OSM stations:

// $env:OSM_IMPORT_ENABLED="true"
// .\mvnw.cmd spring-boot:run

// The importer runs once when Spring Boot starts.