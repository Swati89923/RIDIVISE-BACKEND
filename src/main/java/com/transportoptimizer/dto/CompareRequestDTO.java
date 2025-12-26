package com.transportoptimizer.dto;

import com.transportoptimizer.entity.FareEstimate;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
public class CompareRequestDTO {
    private String userId;
    private String origin;
    private String destination;
    private LocalDateTime departureTime;  // ISO string, optional
    private boolean preferCheapest;
    private boolean preferFastest;
    private Map<String, Object> options;
    private FareEstimate fareEstimate;
    private String chosenProviderId;

}
