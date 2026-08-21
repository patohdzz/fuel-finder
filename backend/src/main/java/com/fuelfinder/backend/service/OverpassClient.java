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
    private static final long REQUEST_SPACING_MILLIS = 3000;

    // Overpass enforces a small number of *concurrent* query slots per IP
    // (its own /api/status reports "Rate limit: 2"), not a simple per-time
    // counter. On a shared host like Railway, other tenants' traffic on
    // the same outbound IP can occupy those slots -- outside anything we
    // control. More attempts with a growing backoff gives contention a
    // real chance to clear instead of retrying into the same busy window.
    private static final int MAX_ATTEMPTS_PER_BOX = 3;
    private static final long RETRY_DELAY_MILLIS = 8000;

    private final RestClient restClient = RestClient.create();

    // regionFilter: blank/null fetches every region; a region name (case-
    // insensitive, e.g. "Arlington") fetches just that one -- lets a
    // region that failed on a previous run be retried on its own, without
    // re-fetching regions that already succeeded.
    public List<String> fetchDfwStations(String regionFilter) {
        List<Region> regionsToFetch = (regionFilter == null || regionFilter.isBlank())
                ? DFW_REGIONS
                : DFW_REGIONS.stream()
                        .filter(region -> region.name().equalsIgnoreCase(regionFilter.trim()))
                        .toList();

        List<String> responses = new ArrayList<>();

        for (int i = 0; i < regionsToFetch.size(); i++) {
            if (i > 0) {
                sleep(REQUEST_SPACING_MILLIS);
            }

            Region region = regionsToFetch.get(i);

            // One region failing after all retries shouldn't discard data
            // we already successfully fetched for the others -- skip it
            // and keep going instead of letting the exception propagate
            // and wipe out everything collected so far.
            try {
                responses.add(fetchWithRetry(region));
            } catch (OverpassApiException e) {
                System.out.println(region.name() + " skipped after all retries failed.");
            }
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
                    sleep(RETRY_DELAY_MILLIS * attempt); // 8s, then 16s
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
