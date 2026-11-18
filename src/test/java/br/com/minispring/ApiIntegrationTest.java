
package br.com.minispring;

import br.com.minispring.exemplo.Application;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(
        classes = Application.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "app.name=Pet Shop de teste"
)
class ApiIntegrationTest {

    @LocalServerPort
    private int port;

    private HttpClient client;

    private final ObjectMapper json = new ObjectMapper();

    @BeforeEach
    void setUp() {
        client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
    }

    private HttpRequest request(
            String method,
            String path,
            String body,
            String contentType
    ) {
        var builder = HttpRequest.newBuilder(
                URI.create("http://127.0.0.1:" + port + path)
        ).timeout(Duration.ofSeconds(10));

        if (contentType != null) {
            builder.header("Content-Type", contentType);
        }

        return builder.method(
                method,
                body == null
                        ? HttpRequest.BodyPublishers.noBody()
                        : HttpRequest.BodyPublishers.ofString(body)
        ).build();
    }

    private HttpResponse<String> send(
            String method,
            String path,
            String body
    ) throws Exception {
        return client.send(
                request(
                        method,
                        path,
                        body,
                        body == null ? null : "application/json"
                ),
                HttpResponse.BodyHandlers.ofString()
        );
    }

    private JsonNode readBody(
            HttpResponse<String> response
    ) throws Exception {
        return json.readTree(response.body());
    }

    @Test
    void fullCrudThroughSpringBoot() throws Exception {

        var info = send("GET", "/", null);

        assertEquals(200, info.statusCode());
        assertEquals(
                "Pet Shop de teste",
                readBody(info).get("application").asText()
        );

        assertEquals(
                200,
                send("GET", "/health", null).statusCode()
        );

        var created = send(
                "POST",
                "/servicos",
                "{\"nome\":\" Banho básico \",\"duracaoMinutos\":40}"
        );

        assertEquals(201, created.statusCode());

        assertTrue(
                created.headers()
                        .firstValue("Content-Type")
                        .orElseThrow()
                        .startsWith("application/json")
        );

        var createdBody = readBody(created);
        long id = createdBody.get("id").asLong();

        assertTrue(id > 0);
        assertEquals("Banho básico", createdBody.get("nome").asText());
        assertEquals(40, createdBody.get("duracaoMinutos").asInt());

        String location = created.headers()
                .firstValue("Location")
                .orElseThrow();

        assertTrue(location.endsWith("/servicos/" + id));

        var fetched = send("GET", "/servicos/" + id, null);

        assertEquals(200, fetched.statusCode());
        assertEquals(
                "Banho básico",
                readBody(fetched).get("nome").asText()
        );

        assertEquals(
                1,
                readBody(
                        send("GET", "/servicos?nome=BANHO", null)
                ).size()
        );

        assertEquals(
                0,
                readBody(
                        send("GET", "/servicos?nome=tosa", null)
                ).size()
        );

        var updated = send(
                "PUT",
                "/servicos/" + id,
                "{\"nome\":\"Banho completo\",\"duracaoMinutos\":80}"
        );

        assertEquals(200, updated.statusCode());

        var updatedBody = readBody(updated);

        assertEquals(id, updatedBody.get("id").asLong());
        assertEquals(
                "Banho completo",
                updatedBody.get("nome").asText()
        );
        assertEquals(
                80,
                updatedBody.get("duracaoMinutos").asInt()
        );

        var removed = send(
                "DELETE",
                "/servicos/" + id,
                null
        );

        assertEquals(204, removed.statusCode());
        assertEquals("", removed.body());

        assertEquals(
                404,
                send("GET", "/servicos/" + id, null).statusCode()
        );

        assertEquals(
                404,
                send("DELETE", "/servicos/" + id, null).statusCode()
        );

        assertEquals(
                404,
                send(
                        "PUT",
                        "/servicos/" + id,
                        "{\"nome\":\"Banho\",\"duracaoMinutos\":20}"
                ).statusCode()
        );
    }

    @Test
    void rejectsInvalidInputs() throws Exception {

        var invalid = send(
                "POST",
                "/servicos",
                "{\"nome\":\" \",\"duracaoMinutos\":0}"
        );

        assertEquals(400, invalid.statusCode());

        for (String body : List.of(
                "{broken",
                "null",
                "",
                "{}",
                "{} {}"
        )) {
            var response = send(
                    "POST",
                    "/servicos",
                    body
            );

            assertEquals(
                    400,
                    response.statusCode(),
                    body + " -> " + response.body()
            );
        }

        var invalidId = send(
                "GET",
                "/servicos/abc",
                null
        );

        assertEquals(400, invalidId.statusCode());

        var tooLong = send(
                "POST",
                "/servicos",
                "{\"nome\":\"Banho\",\"duracaoMinutos\":241}"
        );

        assertEquals(422, tooLong.statusCode());

        var maximum = send(
                "POST",
                "/servicos",
                "{\"nome\":\"Banho especial\",\"duracaoMinutos\":240}"
        );

        assertEquals(201, maximum.statusCode());

        var unsupportedMediaType = client.send(
                request(
                        "POST",
                        "/servicos",
                        "{}",
                        "text/plain"
                ),
                HttpResponse.BodyHandlers.ofString()
        );

        assertEquals(
                415,
                unsupportedMediaType.statusCode()
        );
    }

    @Test
    void distinguishesRouteErrors() throws Exception {

        assertEquals(
                404,
                send("GET", "/inexistente", null).statusCode()
        );

        var response = send(
                "PATCH",
                "/servicos/1",
                "{}"
        );

        assertEquals(405, response.statusCode());

        String allow = response.headers()
                .firstValue("Allow")
                .orElseThrow();

        assertTrue(allow.contains("GET"));
        assertTrue(allow.contains("PUT"));
        assertTrue(allow.contains("DELETE"));
    }

    @Test
    void concurrentRequestsProduceUniqueIds() throws Exception {

        int initialCount = readBody(
                send("GET", "/servicos", null)
        ).size();

        var futures = new ArrayList<
                CompletableFuture<HttpResponse<String>>
                >();

        for (int i = 0; i < 30; i++) {
            futures.add(
                    client.sendAsync(
                            request(
                                    "POST",
                                    "/servicos",
                                    "{\"nome\":\"Banho\",\"duracaoMinutos\":10}",
                                    "application/json"
                            ),
                            HttpResponse.BodyHandlers.ofString()
                    )
            );
        }

        var ids = new HashSet<Long>();

        for (var future : futures) {
            var response = future.join();

            assertEquals(
                    201,
                    response.statusCode(),
                    "Resposta: " + response.body()
            );

            ids.add(
                    readBody(response).get("id").asLong()
            );
        }

        assertEquals(30, ids.size());

        assertEquals(
                initialCount + 30,
                readBody(
                        send("GET", "/servicos", null)
                ).size()
        );
    }
}
