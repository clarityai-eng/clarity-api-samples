package ai.clarity;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.Header;

import java.util.List;
import java.util.Map;

import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

public class HttpRequestHelperTest {

    private static ClientAndServer mockServer;

    @BeforeAll
    public static void startServer() {
        mockServer = startClientAndServer(1080);
    }

    @AfterAll
    public static void stopServer() {
        mockServer.stop();
    }

    @Test
    public void testMapToJsonAndJsonToMap() {
        Map<String, Object> map = Map.of("name", "John", "familyNames", List.of("Smith", "Johnson"));

        var jsonResult = HttpRequestHelper.mapToJson(map).get();

        var mapFromJson = HttpRequestHelper.jsonToMap(jsonResult).get();

        Assertions.assertEquals(map, mapFromJson);
    }

    @Test
    public void testSuccessfulGetRequest() {
        mockServer
                .when(
                        request()
                                .withMethod("GET")
                                .withPath("/get-endpoint"))
                .respond(
                        response()
                                .withStatusCode(200)
                                .withHeaders(
                                        new Header("Content-Type", "text/html; charset=utf-8"),
                                        new Header("Cache-Control", "public, max-age=86400"))
                                .withBody("<html>hello world</html>")
                );

        String response = HttpRequestHelper.getRequest("http://localhost:1080/get-endpoint").get();
        Assertions.assertFalse(response.isEmpty());
    }

    @Test
    public void testSuccessfulGetRequestWithHeaders() {
        mockServer
                .when(
                        request()
                                .withMethod("GET")
                                .withPath("/get-endpoint")
                                .withHeader("Accept", "text/html")
                )
                .respond(
                        response()
                                .withStatusCode(200)
                                .withHeaders(
                                        new Header("Content-Type", "text/html; charset=utf-8"),
                                        new Header("Cache-Control", "public, max-age=86400"))
                                .withBody("<html>hello world with Headers</html>")
                );

        var headers = Map.of("Accept", "text/html");
        String response = HttpRequestHelper.getRequest("http://localhost:1080/get-endpoint", headers).get();
        Assertions.assertFalse(response.isEmpty());
    }

}
