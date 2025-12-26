package com.transportoptimizer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
@AllArgsConstructor
public class ProviderFareDTO {
    private String providerId;
    private String providerName;
    private double price;
    private int etaMinutes;
    private boolean isSurge;
    private Map<String, Object> metadata;
    private String vehicleType;
}
