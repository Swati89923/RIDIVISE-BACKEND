package com.transportoptimizer.entity;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TripRequest {

    private String requestId;
    private String userId;
    private String origin;
    private String destination;
    private LocalDateTime departureTime; // nullable
    private boolean preferCheapest;
    private boolean preferFastest;
    private Map<String, Object> context;
    private Map<String, Object> options;
}
