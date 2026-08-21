package com.fuelfinder.backend.service;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import com.fuelfinder.backend.model.Station;
import com.fuelfinder.backend.repository.StationRepository;

import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class OsmStationImporter {
    // parsing JSON into Station entities

    private final StationRepository stationRepository;
    private final JsonMapper jsonMapper;
    private final OverpassClient overpassClient;


    public OsmStationImporter(
            StationRepository stationRepository,
            JsonMapper jsonMapper,
            OverpassClient overpassClient) {

        this.stationRepository = stationRepository;
        this.jsonMapper = jsonMapper;
        this.overpassClient = overpassClient;
    }

    // regionFilter: blank/null imports all regions; a region name (e.g.
    // "Arlington") imports just that one.
    public void importDfwStations(String regionFilter) throws IOException {
        int inserted = 0;
        int updated = 0;
        int skipped = 0;

        // One response per region (see OverpassClient) -- looped and
        // merged here. A station near a region border could theoretically
        // show up in two responses, but findByOsmTypeAndOsmId makes
        // re-processing it a harmless no-op update, not a duplicate.
        for (String json : overpassClient.fetchDfwStations(regionFilter)) {
            JsonNode root = jsonMapper.readTree(json);
            JsonNode elements = root.get("elements");

            for (JsonNode element : elements) {

                String osmType = element.get("type").asText();
                long osmId = element.get("id").asLong();

                JsonNode tags = element.get("tags");

                if (tags == null) {
                    continue;
                }

                // Only import stations with both a ZIP code and a city --
                // across a multi-city bounding box there's no single safe
                // fallback city to guess for stations OSM doesn't tag.
                if (!tags.has("addr:postcode") || !tags.has("addr:city")) {
                    skipped++;
                    continue;
                }

                // Change the importer from “skip” to “update”
                Station station = stationRepository
                        .findByOsmTypeAndOsmId(osmType, osmId)
                        .orElseGet(Station::new);

                boolean isNewStation = station.getId() == null;

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

                // City is guaranteed present at this point (skipped above otherwise).
                station.setCity(tags.get("addr:city").asText());

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
                if (isNewStation) {
                    inserted++;
                } else {
                    updated++;
                }
            }
        }

        System.out.println(
                "OSM import complete - inserted: " + inserted
                + ", updated: " + updated
                + ", skipped: " + skipped
        );
    }
}
