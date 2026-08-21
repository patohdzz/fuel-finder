package com.fuelfinder.backend.service;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import com.fuelfinder.backend.exception.OverpassApiException;
import org.springframework.web.client.RestClientException;


@Service
public class OverpassClient {

    private static final String OVERPASS_URL = "https://overpass-api.de/api/interpreter";

    // One combined DFW-wide bounding box reliably 504s on the public
    // Overpass server -- confirmed by testing (fails in ~10s, well under
    // our own [timeout:60], so it's the server's dispatcher rejecting the
    // query as too heavy, not a slow response). City-sized boxes succeed
    // individually, so we query Dallas, Fort Worth, and Arlington
    // separately and let the importer merge the results.
    private record Region(String name, String boundingBox) {
    }

    private static final List<Region> DFW_REGIONS = List.of(
            new Region("Dallas", "32.65,-96.95,32.90,-96.65"),
            new Region("Fort Worth", "32.60,-97.50,32.90,-97.20"),
            new Region("Arlington", "32.68,-97.16,32.75,-97.07")
    );

    // Overpass's public server expects clients to space out heavy queries
    // rather than firing several back-to-back -- doing that reliably
    // triggered 504s in testing even though each box succeeded on its own.
    private static final long REQUEST_SPACING_MILLIS = 2000;

    // One retry with a longer backoff, since a 504 here is the server
    // being transiently overloaded, not something a request can fix.
    private static final int MAX_ATTEMPTS_PER_BOX = 2;
    private static final long RETRY_DELAY_MILLIS = 5000;

    private final RestClient restClient = RestClient.create();

    public List<String> fetchDfwStations() {
        List<String> responses = new ArrayList<>();

        for (int i = 0; i < DFW_REGIONS.size(); i++) {
            if (i > 0) {
                sleep(REQUEST_SPACING_MILLIS);
            }

            responses.add(fetchWithRetry(DFW_REGIONS.get(i)));
        }

        return responses;
    }

    private String fetchWithRetry(Region region) {
        OverpassApiException lastFailure = null;

        for (int attempt = 1; attempt <= MAX_ATTEMPTS_PER_BOX; attempt++) {
            System.out.println("Fetching " + region.name() + " (attempt " + attempt + "/" + MAX_ATTEMPTS_PER_BOX + ")...");

            try {
                String result = fetchStationsInBoundingBox(region.boundingBox());
                System.out.println(region.name() + " succeeded.");
                return result;
            } catch (OverpassApiException e) {
                lastFailure = e;
                System.out.println(region.name() + " failed: " + e.getCause().getMessage());

                if (attempt < MAX_ATTEMPTS_PER_BOX) {
                    sleep(RETRY_DELAY_MILLIS);
                }
            }
        }

        throw lastFailure;
    }

    private String fetchStationsInBoundingBox(String boundingBox) {
        String query = """
                [out:json][timeout:60];
                nwr["amenity"="fuel"]
                (%s);
                out center tags;
                """.formatted(boundingBox);

        try {
            return restClient.post()
                    .uri(OVERPASS_URL)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body("data=" + URLEncoder.encode(query, StandardCharsets.UTF_8))
                    .retrieve()
                    .body(String.class);
        } catch (RestClientException e) {
            throw new OverpassApiException("Failed to fetch station data from Overpass API", e);
        }
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
