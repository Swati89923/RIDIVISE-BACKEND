package com.transportoptimizer.util;

import com.transportoptimizer.entity.FareEstimate;
import com.transportoptimizer.entity.ProviderFare;
import com.transportoptimizer.entity.Suggestion;
import com.transportoptimizer.entity.TripRequest;
import com.transportoptimizer.dto.CompareRequestDTO;
import com.transportoptimizer.dto.CompareResponseDTO;
import com.transportoptimizer.dto.ProviderFareDTO;

import java.time.Instant;
import java.util.UUID;
import java.util.stream.Collectors;

public class DtoMapper {

    // ---------------------- FareEstimate + Suggestion → CompareResponseDTO -------------------------
    public static CompareResponseDTO toDto(FareEstimate fareEstimate, Suggestion suggestion, String requestId) {

        return CompareResponseDTO.builder()
                .requestId(requestId)
                .origin(fareEstimate.getOrigin())
                .destination(fareEstimate.getDestination())
                .totalDistanceKm(fareEstimate.getTotalDistanceKm())

                .sortedFares(
                        fareEstimate.getProviderFares().stream()
                                .map(DtoMapper::from)
                                .collect(Collectors.toList())
                )

                .recommendation(
                        suggestion == null ? null :
                                CompareResponseDTO.RecommendationMeta.builder()
                                        .chosenProviderId(suggestion.getChosenProviderId())
                                        .confidenceScore(suggestion.getConfidenceScore())
                                        .reason(suggestion.getReason())
                                        .chosenFare(from(suggestion.getChosenFare()))
                                        .modelRaw(null)
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
    }

    // ---------------------- ProviderFare → ProviderFareDTO -------------------------
    public static ProviderFareDTO from(ProviderFare providerFare) {
        return ProviderFareDTO.builder()
                .providerId(providerFare.getProviderId())
                .providerName(providerFare.getProviderName())
                .price(providerFare.getPrice())
                .etaMinutes(providerFare.getEtaMinutes())
                .isSurge(providerFare.isSurge())
                .metadata(providerFare.getMetadata())
                .vehicleType(providerFare.getVehicleType())
                .build();
    }

    // ---------------------- CompareRequestDTO → TripRequest -------------------------
    public static TripRequest from(CompareRequestDTO dto) {
        return TripRequest.builder()
                .requestId(UUID.randomUUID().toString())
                .userId(dto.getUserId())
                .origin(dto.getOrigin())
                .destination(dto.getDestination())
                .preferCheapest(dto.isPreferCheapest())
                .preferFastest(dto.isPreferFastest())
                .build();
    }
}
