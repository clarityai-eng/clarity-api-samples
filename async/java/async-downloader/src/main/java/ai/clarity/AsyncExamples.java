package ai.clarity;

import java.util.List;
import java.util.Map;

public class AsyncExamples {

    private static final String DEFAULT_KEY = "YOUR_KEY";
    private static final String DEFAULT_SECRET = "YOUR_SECRET";

    private static final String KEY = System.getenv().getOrDefault("CLARITY_AI_API_KEY", DEFAULT_KEY);
    private static final String SECRET = System.getenv().getOrDefault("CLARITY_AI_API_SECRET", DEFAULT_SECRET);

    public static void main(String[] args) {

        var asyncDownloader = new AsyncDownloader("https://api.clarity.ai", KEY, SECRET);

        // Requesting some ESG Risk data for the whole universe of Equities
        Map<String, Object> params = Map.of("scoreIds", List.of("ESG", "ENVIRONMENTAL"),
                                            "securityTypes", List.of("EQUITY"));
        asyncDownloader.download("/securities/esg-risk/scores-by-id/async", params);

        // Requesting SFDR Data for the whole universe of Organizations
        params = Map.of("metricIds", List.of("CARBON_FOOTPRINT", "GHG_INTENSITY"));
        asyncDownloader.download("/organizations/sfdr/metric-by-id/async", params);
    }
}
