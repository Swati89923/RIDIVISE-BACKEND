package com.transportoptimizer.Services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class DistanceMatrixService {

    private final RestTemplate restTemplate = new RestTemplate();

    // Google API key (env var se aayegi)
    @Value("${google.maps.api.key:}")
    private String googleApiKey;

    /**
     * Returns distance in KM between origin and destination.
     * Uses Google Distance Matrix if API key present,
     * otherwise falls back to MOCK distances.
     */
    public double getDistanceKm(String origin, String destination) {

        // 🔁 MOCK MODE (no key)
        if (googleApiKey == null || googleApiKey.isBlank()) {
            double mock = mockDistance(origin, destination);
            log.warn("Google API key missing → using MOCK distance {} km", mock);
            return mock;
        }

        try {
            String url = String.format(
                    "https://maps.googleapis.com/maps/api/distancematrix/json" +
                            "?origins=%s&destinations=%s&units=metric&key=%s",
                    encode(origin),
                    encode(destination),
                    googleApiKey
            );

            Map response = restTemplate.getForObject(url, Map.class);

            if (response == null) {
                log.warn("Google Matrix returned null response");
                return mockDistance(origin, destination);
            }

            String status = (String) response.get("status");
            if (!"OK".equals(status)) {
                log.warn("Google Matrix status: {}", status);
                return mockDistance(origin, destination);
            }

            List rows = (List) response.get("rows");
            if (rows == null || rows.isEmpty()) {
                log.warn("Google Matrix returned empty rows");
                return mockDistance(origin, destination);
            }

            Map row = (Map) rows.get(0);
            List elements = (List) row.get("elements");
            if (elements == null || elements.isEmpty()) {
                log.warn("Google Matrix returned empty elements");
                return mockDistance(origin, destination);
            }

            Map element = (Map) elements.get(0);
            String elementStatus = (String) element.get("status");
            if (!"OK".equals(elementStatus)) {
                log.warn("Google Matrix element status: {}", elementStatus);
                return mockDistance(origin, destination);
            }

            Map distance = (Map) element.get("distance");
            if (distance == null || distance.get("value") == null) {
                log.warn("Google Matrix distance missing");
                return mockDistance(origin, destination);
            }

            double meters = ((Number) distance.get("value")).doubleValue();
            double km = meters / 1000.0;

            log.info("Google Matrix distance {} -> {} = {} km", origin, destination, km);
            return km;

        } catch (Exception e) {
            log.error("Google Matrix failed, using MOCK distance", e);
            return mockDistance(origin, destination);
        }

    }

    // ---------------- MOCK DISTANCE SECTION ----------------

    /**
     * Deterministic mock distances for local testing
     * (add 2–4 locations as you want)
     */
    private double mockDistance(String origin, String destination) {

        String key = (origin + "->" + destination).toLowerCase();

        if (key.contains("delhi") && key.contains("noida")) return 27.5;
        if (key.contains("delhi") && key.contains("rajiv chowk")) return 3.0;
        if (key.contains("mumbai") && key.contains("thane")) return 32.0;
        if (key.contains("bangalore") && key.contains("hyderabad")) return 575.0;
        if (key.contains("delhi") && key.contains("karol bagh")) return 8.7;
        if (key.contains("noida") && key.contains("hyderabad")) return 1686.0;
        if (key.contains("pune") && key.contains("bangalore")) return 845.0;
        if (key.contains("pune") && key.contains("mumbai")) return 156.0;
        if (key.contains("mumbai") && key.contains("hyderabad")) return 742.0;

        // generic deterministic fallback
        return 5 + Math.abs(origin.hashCode() - destination.hashCode()) % 20;
    }

    private String encode(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }
}
