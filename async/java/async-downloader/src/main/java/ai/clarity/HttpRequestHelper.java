package ai.clarity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

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

}
