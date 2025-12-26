package com.transportoptimizer.Services;

import com.transportoptimizer.entity.FareEstimate;
import com.transportoptimizer.entity.ProviderFare;
import com.transportoptimizer.entity.Suggestion;
import com.transportoptimizer.entity.TripRequest;
import com.transportoptimizer.ai.TogetherAIClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class RecommendationService {

    private final TogetherAIClient togetherAIClient;

    public Suggestion recommendBestMode(FareEstimate fareEstimate, TripRequest tripRequest) {
        // 🔥 USER OVERRIDE COMES FIRST
        if (tripRequest.getOptions() != null &&
                tripRequest.getOptions().get("userChosenProviderId") != null) {

            String userChosenId =
                    tripRequest.getOptions().get("userChosenProviderId").toString();

            List<ProviderFare> fares = fareEstimate.getProviderFares();

            if (fares == null || fares.isEmpty()) {
                return Suggestion.builder()
                        .suggestionId("sugg-" + System.currentTimeMillis())
                        .chosenProviderId(null)
                        .chosenFare(null)
                        .confidenceScore(0.0)
                        .reason("No provider fares available")
                        .build();
            }

            ProviderFare userChosenFare = fares.stream()
                    .filter(f -> f.getProviderId().equals(userChosenId))
                    .findFirst()
                    .orElse(null);


            if (userChosenFare != null) {
                return Suggestion.builder()
                        .suggestionId("sugg-" + System.currentTimeMillis())
                        .chosenProviderId(userChosenId)
                        .chosenFare(userChosenFare)
                        .confidenceScore(1.0)
                        .reason("User explicitly selected this option")
                        .build();
            }
        }

        List<ProviderFare> fares = fareEstimate.getProviderFares();


        if (fares == null || fares.isEmpty()) {
            return Suggestion.builder()
                    .suggestionId("sugg-" + System.currentTimeMillis())
                    .chosenProviderId(null)
                    .chosenFare(null)
                    .confidenceScore(0.0)
                    .reason("No provider fares available")
                    .build();
        }

        ProviderFare chosen;

        // Rule 1: cheapest
        if (tripRequest.isPreferCheapest()) {
            chosen = fares.stream()
                    .min(Comparator.comparingDouble(ProviderFare::getPrice))
                    .orElse(null);

            return Suggestion.builder()
                    .suggestionId("sugg-" + System.currentTimeMillis())
                    .chosenProviderId(chosen.getProviderId())
                    .chosenFare(chosen)
                    .confidenceScore(0.85)
                    .reason("Selected because preferCheapest=true (lowest price)")
                    .build();
        }

        // Rule 2: fastest
        if (tripRequest.isPreferFastest()) {
            chosen = fares.stream()
                    .min(Comparator.comparingInt(ProviderFare::getEtaMinutes))
                    .orElse(null);

            return Suggestion.builder()
                    .suggestionId("sugg-" + System.currentTimeMillis())
                    .chosenProviderId(chosen.getProviderId())
                    .chosenFare(chosen)
                    .confidenceScore(0.80)
                    .reason("Selected because preferFastest=true (quickest ETA)")
                    .build();
        }

        // Rule 3: hybrid scoring
        chosen = fares.stream()
                .min(Comparator.comparingDouble(
                        f -> f.getPrice() + f.getEtaMinutes() * 0.2
                ))
                .orElse(null);

        return Suggestion.builder()
                .suggestionId("sugg-" + System.currentTimeMillis())
                .chosenProviderId(chosen.getProviderId())
                .chosenFare(chosen)
                .confidenceScore(0.70)
                .reason("Selected based on hybrid score = price + eta*0.2")
                .build();
    }

    public Suggestion recommendUsingModel(FareEstimate fareEstimate, TripRequest tripRequest) {
        try {
            Map<String, Object> result = togetherAIClient.score(fareEstimate, tripRequest);

            String chosenProviderId = (String) result.get("chosenProviderId");
            Double confidence = result.get("confidence") instanceof Number
                    ? ((Number) result.get("confidence")).doubleValue()
                    : 0.5;

            ProviderFare chosenFare = fareEstimate.getProviderFares()
                    .stream()
                    .filter(f -> f.getProviderId().equals(chosenProviderId))
                    .findFirst()
                    .orElse(null);

            if (chosenFare == null) {
                log.warn("TogetherAI returned unknown providerId {}. Falling back.", chosenProviderId);
                return recommendBestMode(fareEstimate, tripRequest);
            }

            return Suggestion.builder()
                    .suggestionId("sugg-" + System.currentTimeMillis())
                    .chosenProviderId(chosenProviderId)
                    .chosenFare(chosenFare)
                    .confidenceScore(confidence)
                    .reason("AI model recommended best provider with confidence " + confidence)
                    .build();

        } catch (Exception e) {
            log.error("TogetherAI model failed: {}. Falling back to rule-based.", e.getMessage());
            return recommendBestMode(fareEstimate, tripRequest);
        }
    }
}
