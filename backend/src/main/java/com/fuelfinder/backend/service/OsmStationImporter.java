package com.fuelfinder.backend.service;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import com.fuelfinder.backend.model.Station;
import com.fuelfinder.backend.repository.StationRepository;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class OsmStationImporter {

    private final StationRepository stationRepository;
    private final JsonMapper jsonMapper;

    public OsmStationImporter(
            StationRepository stationRepository,
            JsonMapper jsonMapper) {

        this.stationRepository = stationRepository;
        this.jsonMapper = jsonMapper;
    }

    public void importArlingtonStations() throws IOException {

        ClassPathResource resource =
                new ClassPathResource(
                        "data/arlington-gas-stations.json"
                );

        JsonNode root =
                jsonMapper.readTree(resource.getInputStream());

        JsonNode elements = root.get("elements");

        for (JsonNode element : elements) {

            String osmType = element.get("type").asText();
            long osmId = element.get("id").asLong();

            JsonNode tags = element.get("tags");

            if (tags == null) {
                continue;
            }

            // For now, only import stations with ZIP codes.
            if (!tags.has("addr:postcode")) {
                continue;
            }

            // Avoid importing the same OSM station twice.
            if (stationRepository
                    .findByOsmTypeAndOsmId(osmType, osmId)
                    .isPresent()) {

                continue;
            }

            Station station = new Station();

            station.setOsmType(osmType);
            station.setOsmId(osmId);

            /*
             * Name:
             * Prefer the actual OSM name.
             * Otherwise use brand.
             * Otherwise fall back to "Gas Station".
             */
            if (tags.has("name")) {
                station.setName(tags.get("name").asText());
            } else if (tags.has("brand")) {
                station.setName(tags.get("brand").asText());
            } else {
                station.setName("Gas Station");
            }

            /*
             * Address
             */
            String houseNumber =
                    tags.has("addr:housenumber")
                            ? tags.get("addr:housenumber").asText()
                            : "";

            String street =
                    tags.has("addr:street")
                            ? tags.get("addr:street").asText()
                            : "";

            station.setAddress(
                    (houseNumber + " " + street).trim()
            );

            /*
             * These results came from our Arlington test query.
             *
             * If OSM provides city/state, use them.
             * Otherwise use Arlington/TX for THIS initial batch.
             */
            station.setCity(
                    tags.has("addr:city")
                            ? tags.get("addr:city").asText()
                            : "Arlington"
            );

            station.setState(
                    tags.has("addr:state")
                            ? tags.get("addr:state").asText()
                            : "TX"
            );

            station.setZipCode(
                    tags.get("addr:postcode").asText()
            );

            /*
             * Nodes store coordinates directly.
             *
             * Ways/relations returned by our Overpass query
             * store them inside "center".
             */
            if (element.has("lat") && element.has("lon")) {

                station.setLatitude(
                        element.get("lat").asDouble()
                );

                station.setLongitude(
                        element.get("lon").asDouble()
                );

            } else if (element.has("center")) {

                JsonNode center = element.get("center");

                station.setLatitude(
                        center.get("lat").asDouble()
                );

                station.setLongitude(
                        center.get("lon").asDouble()
                );

            } else {
                // Can't use a station without coordinates.
                continue;
            }

            stationRepository.save(station);
        }
    }
}
