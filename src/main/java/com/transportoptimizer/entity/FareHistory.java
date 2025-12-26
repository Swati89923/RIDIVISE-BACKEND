package com.transportoptimizer.entity;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "fare_history")
public class FareHistory {

    @Id
    private String historyId;

    private String userId;
    private String chosenProviderName;
    private TripRequest tripRequest;
    private FareEstimate fareEstimate;
    private String chosenProviderId;
    private ProviderFare chosenFare;
    private Double savings;
    private Double co2EmissionKg;
    private Double walkedDistanceKm;
    private Instant createdAt;
}
