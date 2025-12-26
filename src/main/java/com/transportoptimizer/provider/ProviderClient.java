package com.transportoptimizer.provider;

import com.transportoptimizer.entity.ProviderFare;

import java.util.List;
import java.util.Map;

/**
 * ProviderClient defines the contract for any fare provider integration.
 */
public interface ProviderClient {

    String providerId();
    String providerName();

    /**
     * Fetches a single fare estimate using a precomputed distance.
     *
     * @param origin       trip origin
     * @param destination  trip destination
     * @param distanceKm   distance in kilometers.
     *                     A value {@code <= 0} or {@code NaN} indicates
     *                     an unknown / unavailable distance and implementations
     *                     should treat it as such (e.g. fallback pricing,
     *                     approximation, or neutral estimates).
     */
    ProviderFare getFare(String origin, String destination, double distanceKm);

    /**
     * Backward-compatibility fallback.
     *
     * <p><b>IMPORTANT:</b> This overload does NOT compute distance.
     * It delegates with {@code distanceKm = -1} to indicate
     * an unknown distance. Results may be inaccurate.</p>
     *
     * <p>Callers SHOULD prefer {@link #getFare(String, String, double)}
     * whenever a valid distance is available.</p>
     */
    default ProviderFare getFare(String origin, String destination) {
        return getFare(origin, destination, -1); // unknown distance sentinel
    }

    /**
     * Batch mode with precomputed distance.
     *
     * @param distanceKm distance in kilometers; {@code <= 0} or {@code NaN}
     *                   means unknown distance.
     */
    default List<ProviderFare> getFaresBatch(
            String origin,
            String destination,
            double distanceKm,
            Map<String, Object> options
    ) {
        return List.of(getFare(origin, destination, distanceKm));
    }

    /**
     * Backward-compatibility batch fallback.
     *
     * <p>Delegates with {@code distanceKm = -1} (unknown distance).</p>
     */
    default List<ProviderFare> getFaresBatch(
            String origin,
            String destination,
            Map<String, Object> options
    ) {
        return getFaresBatch(origin, destination, -1, options);
    }
}
