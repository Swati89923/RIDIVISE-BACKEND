package com.transportoptimizer.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProviderFare {

    @JsonProperty("provider_id")
    private String providerId;

    @JsonProperty("provider_name")
    private String providerName;

    @JsonProperty("vehicle_type")
    private String vehicleType;

    @JsonProperty("price")
    private double price;

    @JsonProperty("distance_km")
    private double distanceKm;

    @JsonProperty("eta_minutes")
    private int etaMinutes;

    @JsonProperty("is_surge")
    private boolean isSurge;

    @JsonProperty("currency")
    private String currency;

    @JsonProperty("metadata")
    private Map<String, Object> metadata;
}
