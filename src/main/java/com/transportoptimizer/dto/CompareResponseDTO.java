package com.transportoptimizer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompareResponseDTO {

    private String requestId;
    private String origin;
    private String destination;
    private String snapshotId;   // 👈 VERY IMPORTANT
    private double totalDistanceKm;

    private List<ProviderFareDTO> sortedFares;

    private RecommendationMeta recommendation;   // <-- NEW structured recommendation
    private ResponseMeta meta;                   // <-- response metadata

    // ---------------------------------------------------------------------
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecommendationMeta {
        private String chosenProviderId;
        private double confidenceScore;
        private String reason;
        private ProviderFareDTO chosenFare;
        private Map<String, Object> modelRaw;  // model output if needed
    }

    // ---------------------------------------------------------------------
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResponseMeta {
        private Instant timestamp;
        private String serviceVersion;
        private String traceId;
    }
}
