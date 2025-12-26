package com.transportoptimizer.Controller;

import com.transportoptimizer.Services.CompareSnapshotStore;
import com.transportoptimizer.Services.PriceComparisonServices;
import com.transportoptimizer.Services.RecommendationService;
import com.transportoptimizer.Repository.FareHistoryRepository;

import com.transportoptimizer.dto.ChooseRequest;
import com.transportoptimizer.dto.CompareRequestDTO;
import com.transportoptimizer.dto.CompareResponseDTO;
import com.transportoptimizer.dto.ProviderFareDTO;

import com.transportoptimizer.entity.*;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@RestController
@RequestMapping("/api/v1/compare")
@RequiredArgsConstructor
@Slf4j
public class CompareController {

    private final PriceComparisonServices fareComparisonService;
    private final RecommendationService recommendationService;
    private final FareHistoryRepository fareHistoryRepository;
    private final CompareSnapshotStore snapshotStore;

    // =========================
    // PREVIEW ONLY (NO SAVE)
    // =========================
    @PostMapping
    public ResponseEntity<CompareResponseDTO> compare(
            @Valid @RequestBody CompareRequestDTO dto) {

        try {
            TripRequest trip = TripRequest.builder()
                    .requestId(UUID.randomUUID().toString())
                    .origin(dto.getOrigin())
                    .destination(dto.getDestination())
                    .userId(dto.getUserId())
                    .preferCheapest(dto.isPreferCheapest())
                    .preferFastest(dto.isPreferFastest())
                    .departureTime(dto.getDepartureTime())
                    .build();

            String snapshotId = UUID.randomUUID().toString();

            FareEstimate estimate =fareComparisonService.compareFares(trip);
            snapshotStore.save(snapshotId, estimate);

            Suggestion suggestion =
                    recommendationService.recommendBestMode(estimate, trip);

            CompareResponseDTO response = CompareResponseDTO.builder()
                    .requestId(trip.getRequestId())
                    .snapshotId(snapshotId)
                    .origin(estimate.getOrigin())
                    .destination(estimate.getDestination())
                    .totalDistanceKm(estimate.getTotalDistanceKm())
                    .sortedFares(
                            estimate.getProviderFares().stream()
                                    .map(f -> new ProviderFareDTO(
                                            f.getProviderId(),
                                            f.getProviderName(),
                                            f.getPrice(),
                                            f.getEtaMinutes(),
                                            f.isSurge(),
                                            f.getMetadata(),
                                            f.getVehicleType()
                                    ))
                                    .collect(Collectors.toList())
                    )
                    .recommendation(
                            CompareResponseDTO.RecommendationMeta.builder()
                                    .chosenProviderId(
                                            suggestion.getChosenProviderId())
                                    .confidenceScore(
                                            suggestion.getConfidenceScore())
                                    .reason(suggestion.getReason())
                                    .build()
                    )
                    .meta(
                            CompareResponseDTO.ResponseMeta.builder()
                                    .timestamp(Instant.now())
                                    .serviceVersion("v1.0")
                                    .traceId("TRACE-" + System.currentTimeMillis())
                                    .build()
                    )
                    .build();

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Compare failed", e);
            throw new ResponseStatusException(
                    INTERNAL_SERVER_ERROR, "Compare failed");
        }
    }
    // =========================
// SAVE USER CHOICE ONLY
// =========================
    @PostMapping("/choose")
    public ResponseEntity<Void> saveUserChoice(
            @RequestBody ChooseRequest body) {

        try {
            if (body.getUserId() == null ||
                    body.getSnapshotId() == null ||        // ✅ ADD THIS
                    body.getChosenProviderId() == null) {

                return ResponseEntity.badRequest().build();
            }



            FareEstimate estimate =
                    snapshotStore.get(body.getSnapshotId());

            if (estimate == null) {
                return ResponseEntity.badRequest().build();
            }

            ProviderFare chosenFare = estimate.getProviderFares()
                    .stream()
                    .filter(f -> f.getProviderId()
                            .equals(body.getChosenProviderId()))
                    .findFirst()
                    .orElse(null);

            if (chosenFare == null) {
                return ResponseEntity.badRequest().build();
            }

            double maxPrice = estimate.getProviderFares()
                    .stream()
                    .mapToDouble(ProviderFare::getPrice)
                    .max()
                    .orElse(chosenFare.getPrice());

            double savings = maxPrice - chosenFare.getPrice();

            TripRequest trip = TripRequest.builder()
                    .origin(estimate.getOrigin())
                    .destination(estimate.getDestination())
                    .userId(body.getUserId())
                    .build();

            // Fetch co2
            Map<String, Object> meta = chosenFare.getMetadata();

            Double co2 = null;
            if (meta != null && meta.get("co2EmissionKg") instanceof Number) {
                co2 = ((Number) meta.get("co2EmissionKg")).doubleValue();
            }

            // Walked Distance

            Double walkedDistanceKm = 0.0;

            if ("WALK".equalsIgnoreCase(chosenFare.getVehicleType())) {
                walkedDistanceKm = chosenFare.getDistanceKm();
            }

            FareHistory history = FareHistory.builder()
                    .userId(body.getUserId())
                    .tripRequest(trip)
                    .fareEstimate(estimate)      // 👈 SAME SNAPSHOT
                    .chosenProviderId(chosenFare.getProviderId())
                    .chosenProviderName(chosenFare.getProviderName())
                    .chosenFare(chosenFare) // ProviderFare ka
                    .walkedDistanceKm(walkedDistanceKm)
                    .savings(savings)
                    .co2EmissionKg(co2)
                    .createdAt(Instant.now())
                    .build();

            fareHistoryRepository.save(history);

            snapshotStore.remove(body.getSnapshotId());
            return ResponseEntity.ok().build();

        } catch (Exception e) {
            log.error("Saving user choice failed", e);
            throw new ResponseStatusException(
                    INTERNAL_SERVER_ERROR, "Save failed");
        }
    }
}
