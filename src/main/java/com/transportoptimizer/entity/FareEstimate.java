package com.transportoptimizer.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FareEstimate {

    @JsonProperty("estimate_id")
    private String estimateId;

    @JsonProperty("origin")
    private String origin;

    @JsonProperty("destination")
    private String destination;

    @JsonProperty("total_distance_km")
    private double totalDistanceKm;

    @JsonProperty("provider_fares")
    private List<ProviderFare> providerFares;

    @JsonProperty("timestamp")
    private Instant timestamp;
}
