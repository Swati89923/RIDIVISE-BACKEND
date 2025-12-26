package com.transportoptimizer.ai.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.transportoptimizer.entity.FareEstimate;
import com.transportoptimizer.entity.TripRequest;
import com.transportoptimizer.ai.TogetherAIClient;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class TogetherAIHttpClient implements TogetherAIClient {

    @Value("${togetherai.api.key:REPLACE_WITH_KEY}")
    private String apiKey;

    private final ObjectMapper mapper = new ObjectMapper();
    private final OkHttpClient http = new OkHttpClient();

    @Override
    public Map<String, Object> score(FareEstimate fareEstimate, TripRequest tripRequest) {


        if (apiKey == null || apiKey.equals("REPLACE_WITH_KEY")) {
            log.warn("TogetherAI API key missing — using MOCK result.");

            String firstProvider = fareEstimate.getProviderFares().isEmpty()
                    ? null
                    : fareEstimate.getProviderFares().get(0).getProviderId();

            Map<String, Object> mock = new HashMap<>();
            mock.put("chosenProviderId", firstProvider);
            mock.put("confidence", 0.75);

            return mock;
        }
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("fareEstimate", fareEstimate);
            payload.put("tripRequest", tripRequest);

            RequestBody body = RequestBody.create(
                    mapper.writeValueAsString(payload),
                    MediaType.parse("application/json")
            );

            Request request = new Request.Builder()
                    .url("https://api.together.ai/v1/fare/recommend") // hypothetical endpoint
                    .header("Authorization", "Bearer " + apiKey)
                    .post(body)
                    .build();

            Response response = http.newCall(request).execute();
            String json = response.body().string();

            return mapper.readValue(json, Map.class);

        } catch (Exception e) {
            log.error("TogetherAI API call failed: {}", e.getMessage());
            return new HashMap<>();
        }
    }
}
