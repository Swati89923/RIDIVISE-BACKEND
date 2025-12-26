package com.transportoptimizer.Services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
@RequiredArgsConstructor
public class DistanceService {

    private final DistanceMatrixService distanceMatrixService;

    public double calculateDistanceKm(String origin, String destination) {
        double distance = distanceMatrixService.getDistanceKm(origin, destination);
        log.info("distance {} -> {} = {} km", origin, destination, distance);
        return distance;
    }

    public CompletableFuture<Double> calculateDistanceKmAsync(String origin, String destination) {
        return CompletableFuture.supplyAsync(() -> calculateDistanceKm(origin, destination));
    }
}
