package ai.clarity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HttpRequestHelper {

    private static final Logger logger = Logger.getLogger(HttpRequestHelper.class.getName());

    public static Optional<String> mapToJson(Map<String, Object> map) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return Optional.of(objectMapper.writeValueAsString(map));
        } catch (JsonProcessingException e) {
            logger.log(Level.SEVERE, "Couldn't convert map to JSON");
            return Optional.empty();
        }
    }

    public static Optional<Map<String, Object>> jsonToMap(String json) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            Map<String, Object> mapFromJson = mapper.readValue(json, Map.class);
            return Optional.of(mapFromJson);
        } catch (JsonProcessingException e) {
            logger.log(Level.SEVERE, "Couldn't convert JSON to Map");
            return Optional.empty();
        }
    }

    public static Optional<String> getRequest(String url) {
        return getRequest(url, null);
    }

    public static Optional<String> getRequest(String url, Map<String, String> headers) {
        HttpRequest.Builder requestBuilder = getRequestBuilder(url, headers);
        HttpRequest request = requestBuilder
                .GET()
                .build();

        return handleResponse(request);
    }

    public static Optional<String> postRequest(String url, Map<String, String> headers, String jsonBody) {
        HttpRequest.Builder requestBuilder = getRequestBuilder(url, headers);
        HttpRequest request = requestBuilder
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        return handleResponse(request);
    }

    public static void downloadToFile(String url, Map<String, String> headers, Path path) {
        HttpRequest.Builder requestBuilder = getRequestBuilder(url, headers);
        HttpRequest request = requestBuilder.GET().build();
        HttpClient httpClient = HttpClient.newHttpClient();
        try {
            HttpResponse response = httpClient.send(request, HttpResponse.BodyHandlers.ofFile(path));
            if(response.statusCode() != 200) {
                throw new RuntimeException("Error when downloading file. Status Code: " + response.statusCode());
            }
            else {
                logger.log(Level.INFO, "Successfully downloaded content to file " + path);
            }
        } catch (IOException|InterruptedException e) {
            e.printStackTrace();
            logger.log(Level.SEVERE, "Error when downloading file: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private static HttpRequest.Builder getRequestBuilder(String url, Map<String, String> headers) {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder().uri(URI.create(url));
        if(headers != null) {
            for(String header: headers.keySet()) {
                requestBuilder.header(header,  headers.get(header));
            }
        }
        return requestBuilder;
    }

    private static Optional<String> handleResponse(HttpRequest request) {
        HttpClient httpClient = HttpClient.newHttpClient();
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (!Set.of(200, 202, 302).contains(response.statusCode())) {
                logger.log(Level.SEVERE, "Request finished with status Code: " + response.statusCode());

                if(!response.body().isBlank()) {
                    logger.log(Level.SEVERE, "Error body: " + response.body());
                }

                return Optional.empty();
            }

            return Optional.of(response.body());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            logger.log(Level.SEVERE, "Couldn't make the request. Error: " + e.getMessage());
            return Optional.empty();
        }
    }

}
