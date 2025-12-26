package com.transportoptimizer.provider.impl;

import com.transportoptimizer.entity.ProviderFare;
import com.transportoptimizer.provider.ProviderClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
@Slf4j
@Service
public class RapidoMockClient implements ProviderClient {

    private static final Map<String, Double> BASE_FARE = Map.of(
            "bike", 20.0,
            "auto", 30.0
    );

    private static final Map<String, Double> RATE_PER_KM = Map.of(
            "bike", 3.0,
            "auto", 7.0
    );

    @Override
    public String providerId() {
        return "Rapido";
    }


    @Override
    public String providerName() {
        return "Rapido (Mock)";
    }

    @Override
    public ProviderFare getFare(String origin, String destination, double distance) {
        return buildFare("Rapido Bike", "bike", distance, 3, 6, null);
    }

    @Override
    public List<ProviderFare> getFaresBatch(
            String origin,
            String destination,
            double distance,
            Map<String, Object> options
    ) {
        return List.of(
                buildFare("Rapido Bike", "bike", distance, 3, 6,options),
                buildFare("Rapido Auto", "auto", distance, 4, 8, options)
        );
    }

    private ProviderFare buildFare(
            String name,
            String vehicleType,
            double distance,
            int minEta,
            int maxEta,
            Map<String, Object> options
    )
    {
        double baseFare = BASE_FARE.getOrDefault(vehicleType, 0.0);
        double ratePerKm = RATE_PER_KM.getOrDefault(vehicleType, 0.0);
        double distanceFare = distance * ratePerKm;

        double surgeFactor = calculateSurgeFactor(vehicleType, options);
        boolean surge = surgeFactor > 1.1;

        double finalPrice = (baseFare + distanceFare) * surgeFactor;

        return ProviderFare.builder()
                .providerId(providerId()+" : "+ vehicleType)
                .providerName(providerName())
                .vehicleType(vehicleType)
                .distanceKm(distance)
                .price(finalPrice)
                .etaMinutes((int) random(minEta, maxEta))
                .currency("INR")
                .isSurge(surge)
                .metadata(Map.of(
                        "productName", name,
                        "baseFare", baseFare,
                        "ratePerKm", ratePerKm,
                        "distanceFare", distanceFare,
                        "surgeFactor", surgeFactor,
                        "pricingModel", "base + distance * surge",
                        "source", "mock"
                ))
                .build();
    }
    private double calculateSurgeFactor(
            String vehicleType,
            Map<String, Object> options
    ) {
        // Default: controlled randomness
        if (options == null || !options.containsKey("departureTime")) {
            return random(0.95, 1.15);
        }

        try {
            String timeStr = options.get("departureTime").toString();
            int hour = Integer.parseInt(timeStr.substring(11, 13)); // yyyy-MM-ddTHH:mm

            boolean morningPeak = hour >= 8 && hour <= 11;
            boolean eveningPeak = hour >= 17 && hour <= 21;
            boolean afternoonLow = hour >= 12 && hour <= 16;

            double baseSurge;

            if (morningPeak || eveningPeak) {
                baseSurge = switch (vehicleType) {
                    case "bike" -> 1.05;
                    case "auto" -> 1.15;
                    default -> 1.25;
                };
            } else if (afternoonLow) {
                baseSurge = switch (vehicleType) {
                    case "bike" -> 0.95;
                    case "auto" -> 1.0;
                    default -> 1.05;
                };
            } else {
                // night / early morning
                baseSurge = switch (vehicleType) {
                    case "bike" -> 1.0;
                    case "auto" -> 1.05;
                    default -> 1.1;
                };
            }

            // add slight randomness so it doesn't feel static
            return baseSurge + random(-0.05, 0.05);

        } catch (Exception e) {
            return random(0.95, 1.15);
        }
    }


    private double random(double min, double max) {
        return new Random().nextDouble() * (max - min) + min;
    }
}
