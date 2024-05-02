package ai.clarity;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.matchers.MatchType;
import org.mockserver.matchers.Times;

import java.util.List;
import java.util.Map;

import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.mockserver.model.JsonBody.json;

public class AsyncDownloaderTest {

    private static ClientAndServer mockServer;

    @BeforeAll
    public static void startServer() {
        mockServer = startClientAndServer(1080);

        // Mock requesting a token for all tests
        mockServer
                .when(
                        request()
                                .withMethod("POST")
                                .withPath("/clarity/v1/oauth/token")
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
    }

    @AfterAll
    public static void stopServer() {
        mockServer.stop();
    }

    @Test
    public void testRequestToken() {
        AsyncDownloader asyncDownloader = new AsyncDownloader("http://localhost:1080", "MY_KEY", "MY_SECRET");
        String token = asyncDownloader.requestNewToken();
        Assertions.assertEquals("THE_TOKEN", token);
    }

    @Test
    public void testRequestAsync() {
        mockServer
                .when(
                        request()
                                .withMethod("POST")
                                .withPath("/clarity/v1/public/securities/module/async")
                                .withHeader("Content-type", "application/json")
                                .withHeader("Authorization", "Bearer THE_TOKEN")
                                .withBody(json("{" + System.lineSeparator() +
                                                "    \"scoreIds\": [\"metric1\"]," + System.lineSeparator() +
                                                "    \"securityTypes\": [\"EQUITY\"]" + System.lineSeparator() +
                                                "}",
                                        MatchType.STRICT
                                ))
                )
                .respond(
                        response()
                                .withStatusCode(200)
                                .withBody("{\"uuid\": \"NEW_JOB_ID\"}")
                );

        AsyncDownloader asyncDownloader = new AsyncDownloader("http://localhost:1080", "MY_KEY", "MY_SECRET");
        Map<String, Object> params = Map.of("scoreIds", List.of("metric1"),
                                            "securityTypes", List.of("EQUITY"));
        String jobId = asyncDownloader.requestAsync("/securities/module/async", params);
        Assertions.assertEquals("NEW_JOB_ID", jobId);
    }

    @Test
    public void testWaitForJobUntilSuccess() {
        mockServer
                .when(
                        request()
                                .withMethod("GET")
                                .withPath("/clarity/v1/public/job/MY_JOB_ID/status")
                                .withHeader("Content-Type", "application/json")
                                .withHeader("Authorization", "Bearer THE_TOKEN"),
                        Times.exactly(5)
                )
                .respond(
                        response()
                                .withStatusCode(202)
                                .withBody("{\"statusMessage\": \"RUNNING\"}")
                );

        mockServer
                .when(
                        request()
                                .withMethod("GET")
                                .withPath("/clarity/v1/public/job/MY_JOB_ID/status")
                                .withHeader("Content-Type", "application/json")
                                .withHeader("Authorization", "Bearer THE_TOKEN")
                )
                .respond(
                        response()
                                .withStatusCode(302)
                                .withBody("{\"statusMessage\": \"SUCCESS\"}")
                );

        AsyncDownloader asyncDownloader = new AsyncDownloader("http://localhost:1080", "MY_KEY", "MY_SECRET", 1);
        asyncDownloader.waitForJob("MY_JOB_ID");
    }

    @Test
    public void testWaitForJobUntilFail() {
        mockServer
                .when(
                        request()
                                .withMethod("GET")
                                .withPath("/clarity/v1/public/job/MY_JOB_ID/status")
                                .withHeader("Content-Type", "application/json")
                                .withHeader("Authorization", "Bearer THE_TOKEN"),
                        Times.exactly(5)
                )
                .respond(
                        response()
                                .withStatusCode(202)
                                .withBody("{\"statusMessage\": \"RUNNING\"}")
                );

        mockServer
                .when(
                        request()
                                .withMethod("GET")
                                .withPath("/clarity/v1/public/job/MY_JOB_ID/status")
                                .withHeader("Content-Type", "application/json")
                                .withHeader("Authorization", "Bearer THE_TOKEN")
                )
                .respond(
                        response()
                                .withStatusCode(422)
                                .withBody("{\"statusMessage\": \"ERROR\"}")
                );

        AsyncDownloader asyncDownloader = new AsyncDownloader("http://localhost:1080", "MY_KEY", "MY_SECRET", 1);
        Assertions.assertThrows(RuntimeException.class ,() -> asyncDownloader.waitForJob("MY_JOB_ID"));
    }

}
