package com.transportoptimizer.entity;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Suggestion {

    private String suggestionId;
    private String chosenProviderId;
    private double savings;
    private ProviderFare chosenFare;
    private double confidenceScore;
    private String reason;
}
