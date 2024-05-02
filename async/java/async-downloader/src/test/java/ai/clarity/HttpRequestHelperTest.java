package ai.clarity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

public class HttpRequestHelperTest {

    @Test
    public void testConvertToJson() throws JsonProcessingException {
        Map<String, Object> map = Map.of("name", "John", "familyNames", List.of("Smith", "Johnson"));

        var jsonResult = HttpRequestHelper.convertToJson(map).get();

        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> mapFromJson = mapper.readValue(jsonResult, Map.class);
        Assertions.assertEquals(map, mapFromJson);
    }

}
