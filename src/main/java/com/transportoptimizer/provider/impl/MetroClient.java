package com.transportoptimizer.provider.impl;

import com.transportoptimizer.Services.MetroCoverageService;
import com.transportoptimizer.entity.ProviderFare;
import com.transportoptimizer.provider.ProviderClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class MetroClient implements ProviderClient {

    private final MetroCoverageService metroCoverageService;

    @Override
    public String providerId() {
        return "Metro";
    }

    @Override
    public String providerName() {
        return "Metro";
    }

    @Override
    public ProviderFare getFare(String origin, String destination, double distance) {

        //  STRICT station-level check
        if (!metroCoverageService.isMetroRoute(origin, destination)) {
            log.info("Metro NOT available for route: {} -> {}", origin, destination);
            return null;
        }

        double price = 10 + (distance * 5)/1.5;
        int eta = (int) Math.max(5, distance / 0.4);

        return ProviderFare.builder()
                .providerId(providerId())
                .providerName("Metro")
                .vehicleType("metro")
                .distanceKm(distance)
                .price(price)
                .etaMinutes(eta)
                .currency("INR")
                .isSurge(false)
                .metadata(Map.of("network", "city-metro"))
                .build();
    }

    @Override
    public List<ProviderFare> getFaresBatch(
            String origin,
            String destination,
            double distance,
            Map<String, Object> options
    ) {
        ProviderFare fare = getFare(origin, destination, distance);
        return fare == null ? List.of() : List.of(fare);
    }
}
