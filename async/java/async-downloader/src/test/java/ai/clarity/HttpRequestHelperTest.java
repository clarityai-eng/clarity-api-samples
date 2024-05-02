package ai.clarity;

import org.junit.jupiter.api.*;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.Header;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.mockserver.model.StringBody.exact;

public class HttpRequestHelperTest {

    private static ClientAndServer mockServer;

    @BeforeEach
    public void startServer() {
        mockServer = startClientAndServer(1080);
    }

    @AfterEach
    public void stopServer() {
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
        Assertions.assertEquals("<html>hello world</html>", response);
    }

    @Test
    public void testSuccessfulGetRequestWithHeaders() {
        mockServer
                .when(
                        request()
                                .withMethod("GET")
                                .withPath("/get-endpoint-with-headers")
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
        String response = HttpRequestHelper.getRequest("http://localhost:1080/get-endpoint-with-headers", headers).get();
        Assertions.assertEquals("<html>hello world with Headers</html>", response);
    }

    @Test
    public void testSuccessfulPostRequest() {
        mockServer
                .when(
                        request()
                                .withMethod("POST")
                                .withPath("/post-endpoint")
                                .withHeader("Content-type", "application/json")
                                .withBody(exact("{\"param1\":\"value1\"}"))
                )
                .respond(
                        response()
                                .withStatusCode(200)
                                .withBody("OK")
                );

        var headers = Map.of("Content-type", "application/json");
        var jsonBody = HttpRequestHelper.mapToJson(Map.of("param1", "value1")).get();
        String response = HttpRequestHelper.postRequest("http://localhost:1080/post-endpoint", headers, jsonBody).get();
        Assertions.assertEquals("OK", response);
    }

    @Test
    public void testRequestWithError() {
        mockServer
                .when(
                        request()
                                .withMethod("GET")
                                .withPath("/get-endpoint-with-error")
                )
                .respond(
                        response()
                                .withStatusCode(401)
                                .withBody("Not Authorized")
                );

        Optional<String> response = HttpRequestHelper.getRequest("http://localhost:1080/get-endpoint-with-error");
        Assertions.assertTrue(response.isEmpty());
    }

    @Test
    public void testDownloadContentToFile() throws IOException {
        mockServer
                .when(
                        request()
                                .withMethod("GET")
                                .withPath("/download")
                )
                .respond(
                        response()
                                .withStatusCode(200)
                                .withBody("This is a big body")
                );

        var tmpFile = Files.createTempFile("test", ".txt");
        HttpRequestHelper.downloadToFile("http://localhost:1080/download", null, tmpFile);
        Assertions.assertNotNull(tmpFile);
    }
}
