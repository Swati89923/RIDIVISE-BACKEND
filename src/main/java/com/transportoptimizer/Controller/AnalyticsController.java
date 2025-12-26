package com.transportoptimizer.Controller;

import com.transportoptimizer.entity.FareHistory;
import com.transportoptimizer.Repository.FareHistoryRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final FareHistoryRepository fareHistoryRepository;


    @GetMapping("/summary")
    public Map<String, Object> summary(@RequestParam String userId) {

        List<FareHistory> history =
                fareHistoryRepository.findByUserIdOrderByCreatedAtDesc(userId);

        // 🔹 OLD EMPTY CASE (unchanged, just extended)
        if (history.isEmpty()) {
            return Map.of(
                    "totalRequests", 0,
                    "averageSavings", 0.0,
                    "mostUsedProvider", "N/A",
                    "co2SavedKg", 0.0,
                    "walkedKm", 0.0,
                    "caloriesBurned", 0
            );
        }

        int totalRequests = history.size();

        double avgSavings = history.stream()
                .filter(h -> h.getSavings() != null)
                .mapToDouble(FareHistory::getSavings)
                .average()
                .orElse(0.0);

        String mostUsedProvider = history.stream()
                .collect(Collectors.groupingBy(
                        FareHistory::getChosenProviderId,
                        Collectors.counting()
                ))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("N/A");


        Instant cutoff = Instant.now().minusSeconds(30L * 24 * 60 * 60);

        double totalCo2 = 0.0;
        double totalWalkedKm = 0.0;

        for (FareHistory h : history) {
            if (h.getCreatedAt().isBefore(cutoff)) continue;

            if (h.getCo2EmissionKg() != null) {
                totalCo2 += h.getCo2EmissionKg();
            }

            if (h.getWalkedDistanceKm() != null) {
                totalWalkedKm += h.getWalkedDistanceKm();
            }
        }

        double caloriesBurned = totalWalkedKm * 50.0;

        return Map.of(
                "totalRequests", totalRequests,
                "averageSavings", round(avgSavings),
                "mostUsedProvider", mostUsedProvider,
                "co2SavedKg", round(totalCo2),
                "walkedKm", round(totalWalkedKm),
                "caloriesBurned", Math.round(caloriesBurned)
        );
    }

    // helper (NEW, safe addition)
    private double round(double v) {
        return Math.round(v * 100.0) / 100.0;
    }

    //  (Minimal addition for Provider-wise Savings Graph)
    @GetMapping("/provider-savings-trend")
    public Map<String, Object> providerSavingsTrend(@RequestParam String userId) {

        List<FareHistory> history = fareHistoryRepository
                .findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .filter(h -> h.getSavings() != null)
                .collect(Collectors.toList());

        if (history.isEmpty()) {
            return Map.of(
                    "labels", List.of(),
                    "datasets", List.of()
            );
        }

        // oldest → newest
        Collections.reverse(history);

        List<String> labels = new ArrayList<>();
        int c = 1;
        for (int i = 0; i < history.size(); i++) {
            labels.add("#" + c++);
        }

        Map<String, List<Double>> providerSavings = new HashMap<>();
        Map<String, String> providerNames = new HashMap<>();

        for (FareHistory h : history) {
            String pid = h.getChosenProviderName();
            if (pid == null) continue;

            String name = h.getChosenFare().getProviderName();

            providerSavings.putIfAbsent(pid, new ArrayList<>());
            providerSavings.get(pid).add(h.getSavings());

            providerNames.put(pid, name);
        }

        // pad missing values → keep equal length
        providerSavings.forEach((pid, list) -> {
            while (list.size() < labels.size()) list.add(null);
        });

        List<Map<String, Object>> datasets = new ArrayList<>();
        for (String pid : providerSavings.keySet()) {
            datasets.add(Map.of(
                    "label", providerNames.get(pid),
                    "data", providerSavings.get(pid)
            ));
        }

        return Map.of(
                "labels", labels,
                "datasets", datasets
        );
    }

    @GetMapping("/co2-trend")
    public Map<String, Object> co2Trend(
            @RequestParam String userId,
            @RequestParam(defaultValue = "7") int days
    ) {

        List<FareHistory> history =
                fareHistoryRepository.findByUserIdOrderByCreatedAtDesc(userId);

        if (history.isEmpty()) {
            return Map.of(
                    "labels", List.of(),
                    "data", List.of()
            );
        }

        ZoneId zone = ZoneId.systemDefault();

        // Use LocalDate instead of seconds math
        LocalDate cutoffDate = LocalDate.now(zone).minusDays(days - 1);

        Map<String, Double> co2PerDay = history.stream()
                .filter(h -> h.getCo2EmissionKg() != null)
                .filter(h -> {
                    LocalDate d = h.getCreatedAt().atZone(zone).toLocalDate();
                    return !d.isBefore(cutoffDate);
                })
                .collect(Collectors.groupingBy(
                        h -> h.getCreatedAt().atZone(zone).toLocalDate().toString(),
                        TreeMap::new,
                        Collectors.summingDouble(FareHistory::getCo2EmissionKg)
                ));

        return Map.of(
                "labels", new ArrayList<>(co2PerDay.keySet()),
                "data", new ArrayList<>(co2PerDay.values())
        );
    }



    // Walked distance and calories burnt graph

    @GetMapping("/walking-trend")
    public Map<String, Object> walkingTrend(
            @RequestParam String userId,
            @RequestParam(defaultValue = "week") String range
    ) {
        ZoneId zone = ZoneId.systemDefault();
        List<FareHistory> history =
                fareHistoryRepository.findByUserIdOrderByCreatedAtDesc(userId);

        if (history.isEmpty()) {
            return Map.of(
                    "labels", List.of(),
                    "distance", List.of(),
                    "calories", List.of()
            );
        }

        // 🔹 Decide grouping key
        Map<String, Double> walkedPerBucket = new TreeMap<>();

        for (FareHistory h : history) {
            if (h.getWalkedDistanceKm() == null || h.getWalkedDistanceKm() <= 0) continue;

            String key;

            if ("day".equalsIgnoreCase(range)) {
                LocalDate date = h.getCreatedAt().atZone(zone).toLocalDate();
                key = date.toString(); // yyyy-MM-dd
            }
            else if ("month".equalsIgnoreCase(range)) {
                LocalDate date = h.getCreatedAt().atZone(zone).toLocalDate();
                key = date.getYear() + "-" + String.format("%02d", date.getMonthValue());
                // yyyy-MM
            }
            else {
                // week (ISO week)
                LocalDate date = h.getCreatedAt().atZone(zone).toLocalDate();
                WeekFields wf = WeekFields.ISO;
                int week = date.get(wf.weekOfWeekBasedYear());
                int year = date.get(wf.weekBasedYear());
                key = year + "-W" + week;

            }

            walkedPerBucket.merge(key, h.getWalkedDistanceKm(), Double::sum);
        }

        // 🔹 Calories calculation
        final double CALORIES_PER_KM = 50.0;

        List<String> labels = new ArrayList<>(walkedPerBucket.keySet());
        List<Double> distance = new ArrayList<>();
        List<Double> calories = new ArrayList<>();

        for (Double km : walkedPerBucket.values()) {
            distance.add(km);
            calories.add(km * CALORIES_PER_KM);
        }

        return Map.of(
                "labels", labels,
                "distance", distance,
                "calories", calories
        );
    }
    @GetMapping("/savings-vs-co2")
    public Map<String, Object> savingsVsCo2(@RequestParam String userId) {

        List<FareHistory> history =
                fareHistoryRepository.findByUserIdOrderByCreatedAtDesc(userId);

        List<Map<String, Object>> points = new ArrayList<>();

        for (FareHistory h : history) {
            if (h.getSavings() == null || h.getCo2EmissionKg() == null) continue;

            points.add(Map.of(
                    "x", h.getSavings(),          // Savings (₹)
                    "y", h.getCo2EmissionKg()     // CO₂ (kg)
            ));
        }

        return Map.of(
                "points", points
        );
    }


}
