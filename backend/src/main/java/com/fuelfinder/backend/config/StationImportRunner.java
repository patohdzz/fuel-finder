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
    public void run(String... args) throws Exception {
        osmStationImporter.importArlingtonStations();
    }
}

// Normal FuelFinder startup:

// .\mvnw.cmd spring-boot:run

// No import happens.

// When you intentionally want to refresh OSM stations:

// $env:OSM_IMPORT_ENABLED="true"
// .\mvnw.cmd spring-boot:run

// The importer runs once when Spring Boot starts.