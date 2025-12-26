package com.transportoptimizer.provider.impl;

import com.transportoptimizer.entity.ProviderFare;
import com.transportoptimizer.provider.ProviderClient;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class WalkClient implements ProviderClient {

    private static final double MAX_WALK_DISTANCE_KM = 3.0;

    @Override
    public String providerId() {
        return "Walk";
    }

    @Override
    public String providerName() {
        return "Walk";
    }
    @Override
    public ProviderFare getFare(String origin, String destination, double distanceKm) {
        // delegate to batch (single option)
        return getFaresBatch(origin, destination, distanceKm, null)
                .stream()
                .findFirst()
                .orElse(null);
    }

    @Override
    public List<ProviderFare> getFaresBatch(
            String origin,
            String destination,
            double distanceKm,
            Map<String, Object> options
    ) {
        // distance invalid or too long → no walk option
        if (distanceKm <= 0 || distanceKm > MAX_WALK_DISTANCE_KM) {
            return List.of();
        }

        // avg walking speed ~ 5 km/h → 12 min per km
        int etaMinutes = (int) Math.ceil(distanceKm * 12);

        double co2SavedKg = distanceKm * 0.12; // approx cab baseline

        return List.of(
                ProviderFare.builder()
                        .providerId(providerId())
                        .providerName("Walk Your Path")
                        .vehicleType("walk")
                        .distanceKm(distanceKm)
                        .price(0.0)
                        .etaMinutes(etaMinutes)
                        .currency("INR")
                        .isSurge(false)
                        .metadata(Map.of(
                                "co2EmissionKg", 0.0,
                                "co2SavedKg", co2SavedKg,
                                "healthBenefit", "high",
                                "recommendedFor", "short distances"
                        ))
                        .build()
        );
    }
}
