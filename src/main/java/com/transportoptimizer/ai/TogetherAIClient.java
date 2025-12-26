package com.transportoptimizer.ai;

import com.transportoptimizer.entity.FareEstimate;
import com.transportoptimizer.entity.TripRequest;

import java.util.Map;

public interface TogetherAIClient {

    /**
     * Scores provider options using an external AI model.
     * Should return a map containing at least:
     * - chosenProviderId (String)
     * - confidence (Double 0..1)
     *
     * If model fails or is unavailable, implementation may fallback.
     */
    Map<String, Object> score(FareEstimate fareEstimate, TripRequest tripRequest);
}
