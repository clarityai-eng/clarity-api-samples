package ai.clarity;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

public class HttpRequestHelperTest {

    @Test
    public void testMapToJsonAndJsonToMap() {
        Map<String, Object> map = Map.of("name", "John", "familyNames", List.of("Smith", "Johnson"));

        var jsonResult = HttpRequestHelper.mapToJson(map).get();

        var mapFromJson = HttpRequestHelper.jsonToMap(jsonResult).get();

        Assertions.assertEquals(map, mapFromJson);
    }

    @Test
    public void testSuccessfulGetRequest() {
        String response = HttpRequestHelper.getRequest("https://developer.clarity.ai/").get();
        Assertions.assertFalse(response.isEmpty());
    }

    @Test
    public void testSuccessfulGetRequestWithHeaders() {
        var headers = Map.of("Accept", "text/html");
        String response = HttpRequestHelper.getRequest("https://developer.clarity.ai/", headers).get();
        Assertions.assertFalse(response.isEmpty());
    }
}
