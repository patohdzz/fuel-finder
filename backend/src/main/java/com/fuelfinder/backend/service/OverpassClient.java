package com.fuelfinder.backend.service;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import com.fuelfinder.backend.exception.OverpassApiException;
import org.springframework.web.client.RestClientException;


@Service
public class OverpassClient {

    private static final String OVERPASS_URL = "https://overpass-api.de/api/interpreter";

    private final RestClient restClient = RestClient.create();


    public String fetchArlingtonStations() {
        String query = """
                [out:json][timeout:25];
                nwr["amenity"="fuel"]
                (32.68,-97.16,32.75,-97.07);
                out center tags;
                """;


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
}
