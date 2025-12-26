package com.transportoptimizer.Controller;

import com.transportoptimizer.entity.FareHistory;
import com.transportoptimizer.Repository.FareHistoryRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/history")
@RequiredArgsConstructor
public class HistoryController {

    private final FareHistoryRepository fareHistoryRepository;
    @GetMapping("/{userId}")
    public List<Map<String, Object>> getHistory(@PathVariable String userId) {
        List<FareHistory> history = fareHistoryRepository.findByUserIdOrderByCreatedAtDesc(userId);

        return history.stream().map(h -> {
            Map<String, Object> dto = new HashMap<>();

            dto.put("userId", h.getUserId());
            dto.put("tripRequest", h.getTripRequest());
            dto.put("origin", h.getTripRequest().getOrigin());
            dto.put("destination", h.getTripRequest().getDestination());

            // ⭐ Correct format: ARRAY of provider fares
            dto.put("fareEstimate", h.getFareEstimate().getProviderFares());

            dto.put("co2EmissionKg", h.getCo2EmissionKg());
            dto.put("chosenProviderId", h.getChosenProviderId());
            dto.put("savings", h.getSavings());
            dto.put("createdAt", h.getCreatedAt());

            return dto;
        }).toList();
    }


    @PostMapping
    public FareHistory saveHistory(@RequestBody FareHistory history) {
        history.setCreatedAt(Instant.now());
        return fareHistoryRepository.save(history);
    }
}
