package ai.clarity;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.matchers.MatchType;

import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.mockserver.model.JsonBody.json;

public class AsyncDownloaderTest {

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
    public void testRequestToken() {
        mockServer
                .when(
                        request()
                                .withMethod("POST")
                                .withPath("/oauth/token")
                                .withHeader("Content-type", "application/json")
                                .withBody(json("{" + System.lineSeparator() +
                                                "    \"key\": \"MY_KEY\"," + System.lineSeparator() +
                                                "    \"secret\": \"MY_SECRET\"," + System.lineSeparator() +
                                                "}",
                                        MatchType.STRICT
                                ))
                )
                .respond(
                        response()
                                .withStatusCode(200)
                                .withBody("{\"token\": \"THE_TOKEN\"}")
                );

        AsyncDownloader asyncDownloader = new AsyncDownloader("http://localhost:1080", "MY_KEY", "MY_SECRET");
        String token = asyncDownloader.requestToken();
        Assertions.assertEquals("THE_TOKEN", token);
    }
}
