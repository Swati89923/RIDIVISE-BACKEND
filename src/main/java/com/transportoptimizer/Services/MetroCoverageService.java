package com.transportoptimizer.Services;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class MetroCoverageService {

    /**
     * City -> List of metro stations (keywords)
     * Keep names short & common (as users type)
     */
    private static final Map<String, List<String>> METRO_STATIONS = Map.of(
            "delhi-ncr", List.of(
                    "rajiv chowk", "kashmere gate", "new delhi","delhi",
                    "aiims", "hauz khas", "green park",
                    "dilshad garden", "rohini west", "chandni chowk"
            ),
            "mumbai", List.of(
                    "borivali", "kandivali", "malad",
                    "andheri", "goregaon"
            ),
            "bengaluru", List.of(
                    "silk board", "jayadeva", "jp nagar",
                    "iim bangalore", "hulimavu", "hebbagodi"
            ),
            "kolkata", List.of(
                    "esplanade", "howrah", "dum dum", "tollygunge"
            ),
            "chennai", List.of(
                    "koyambedu", "cmbt", "arumbakkam"
            ),
            "hyderabad", List.of(
                    "ameerpet", "begumpet", "miyapur"
            ),
            "lucknow", List.of(
                    "charbagh", "hazratganj", "ccs airport"
            )
    );

    public boolean isMetroRoute(String origin, String destination) {
        String o = origin.toLowerCase();
        String d = destination.toLowerCase();

        for (Map.Entry<String, List<String>> entry : METRO_STATIONS.entrySet()) {
            boolean originMatch = containsAny(o, entry.getValue());
            boolean destinationMatch = containsAny(d, entry.getValue());

            if (originMatch && destinationMatch) {
                return true; // ✅ same city metro stations
            }
        }
        return false;
    }

    private boolean containsAny(String input, List<String> stations) {
        return stations.stream().anyMatch(input::contains);
    }
}
