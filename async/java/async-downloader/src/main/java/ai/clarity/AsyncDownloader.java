package ai.clarity;

import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AsyncDownloader {

    private static final Logger logger = Logger.getLogger(AsyncDownloader.class.getName());

    private String token;
    private final String domain;
    private final String key;
    private final String secret;

    public AsyncDownloader(String domain, String key, String secret) {
        this.domain = domain;
        this.key = key;
        this.secret = secret;
    }

    public String requestNewToken() {
        Map<String, String> headers = Map.of("Content-type", "application/json");

        Map<String, Object> bodyParams = Map.of("key", this.key, "secret", this.secret);
        var jsonBody = HttpRequestHelper.mapToJson(bodyParams).get();

        var url = domain + "/clarity/v1/oauth/token";
        logger.log(Level.INFO, "Requesting new token to " + url);
        Optional<String> response = HttpRequestHelper.postRequest(url, headers, jsonBody);
        if(response.isEmpty()){
            throw new RuntimeException("Couldn't request the access token");
        }
        Map<String, Object> responseMap = HttpRequestHelper.jsonToMap(response.get()).get();
        return (String) responseMap.get("token");
    }

    public String requestAsync(String apiPath, Map<String, Object> data) {
        String url = getUrl(apiPath);
        Map<String, String> headers = getHeaders();
        String jsonData = HttpRequestHelper.mapToJson(data).get();

        logger.log(Level.INFO, "Requesting Job to " + url + " with data " + jsonData);

        var response = HttpRequestHelper.postRequest(url, headers, jsonData);

        String jobId = null;
        if(response.isPresent()) {
            Map<String, Object> responseMap = HttpRequestHelper.jsonToMap(response.get()).get();

            if(responseMap.containsKey("uuid")) {
                jobId = (String) responseMap.get("uuid");
            }
        }

        if(jobId == null) {
            throw new RuntimeException("The request for an async job failed");
        }
        else {
            logger.info("Requested Job with UUID:" + jobId);
            return jobId;
        }

    }

    private String getToken() {
        if(this.token == null) {
            this.token = requestNewToken();
        }
        return this.token;
    }

    private Map<String, String> getHeaders() {
        return Map.of("Content-Type", "application/json",
                      "Authorization", "Bearer " + getToken());
    }

    private String getUrl(String apiPath) {
        return domain + "/clarity/v1/public" + apiPath;
    }
}
