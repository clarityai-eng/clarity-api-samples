package ai.clarity;

import java.util.Map;

public class AsyncDownloader {
    private String token;
    private final String domain;
    private final String key;
    private final String secret;

    public AsyncDownloader(String domain, String key, String secret) {
        this.domain = domain;
        this.key = key;
        this.secret = secret;
    }

    public String requestToken() {
        Map<String, String> headers = Map.of("Content-type", "application/json");

        Map<String, Object> bodyParams = Map.of("key", this.key, "secret", this.secret);
        var jsonBody = HttpRequestHelper.mapToJson(bodyParams).get();

        var response = HttpRequestHelper.postRequest(getUrl("/oauth/token"), headers, jsonBody).get();
        Map<String, Object> responseMap = HttpRequestHelper.jsonToMap(response).get();
        return (String) responseMap.get("token");
    }

    private String getUrl(String apiPath) {
        return domain + apiPath;
    }
}
