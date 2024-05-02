package ai.clarity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HttpRequestHelper {

    private static Logger logger = Logger.getLogger(HttpRequestHelper.class.getName());

    public static Optional<String> mapToJson(Map<String, Object> map) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return Optional.of(objectMapper.writeValueAsString(map));
        } catch (JsonProcessingException e) {
            logger.log(Level.SEVERE, "Couldn't covert map to JSON");
            return Optional.empty();
        }
    }

    public static Optional<Map<String, Object>> jsonToMap(String json) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            Map<String, Object> mapFromJson = mapper.readValue(json, Map.class);
            return Optional.of(mapFromJson);
        } catch (JsonProcessingException e) {
            logger.log(Level.SEVERE, "Couldn't covert JSON to Map");
            return Optional.empty();
        }
    }

    public static Optional<String> getRequest(String url) {
        return getRequest(url, null);
    }

    public static Optional<String> getRequest(String url, Map<String, String> headers) {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder().uri(URI.create(url));
        if(headers != null) {
            requestBuilder = addHeaders(requestBuilder, headers);
        }

        HttpRequest request = requestBuilder
                .GET()
                .build();

        return handleResponse(request);
    }

    private static HttpRequest.Builder addHeaders(HttpRequest.Builder requestBuilder, Map<String, String> headers) {
        for(String header: headers.keySet()) {
            requestBuilder.header(header,  headers.get(header));
        }
        return requestBuilder;
    }

    private static Optional<String> handleResponse(HttpRequest request) {
        HttpClient httpClient = HttpClient.newHttpClient();
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                logger.log(Level.SEVERE, "Request finished with status Code: " + response.statusCode());
                return Optional.empty();
            }

            return Optional.of(response.body());
        } catch (IOException | InterruptedException e) {
            logger.log(Level.SEVERE, "Couldn't make the request. Error: " + e.getMessage());
            return Optional.empty();
        }
    }

}
