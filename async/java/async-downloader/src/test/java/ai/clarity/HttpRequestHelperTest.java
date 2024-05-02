package ai.clarity;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

public class HttpRequestHelperTest {

    @Test
    public void testMapToJsonAndJsonToMap() throws JsonProcessingException {
        Map<String, Object> map = Map.of("name", "John", "familyNames", List.of("Smith", "Johnson"));

        var jsonResult = HttpRequestHelper.mapToJson(map).get();

        var mapFromJson = HttpRequestHelper.jsonToMap(jsonResult).get();

        Assertions.assertEquals(map, mapFromJson);
    }

}
