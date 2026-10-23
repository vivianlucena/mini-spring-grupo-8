package br.com.minispring;

import br.com.minispring.framework.MiniApplication;
import br.com.minispring.framework.context.AppConfig;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;

class ApiIntegrationTest {

    private MiniApplication app;
    private HttpClient client;
    private final ObjectMapper json = new ObjectMapper();

    @BeforeEach
    void start() throws Exception {
        var properties = new Properties();
        properties.setProperty("app.name", "Pet Shop de teste");

        app = MiniApplication.start(
                "br.com.minispring.exemplo",
                new AppConfig(properties, Map.of()),
                0
        );

        client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
    }

    @AfterEach
    void stop() throws Exception {
        if (client != null) {
            client.close();
        }
        if (app != null) {
            app.close();
        }
    }

    private HttpRequest request(String method, String path, String body, String contentType) {
        var builder = HttpRequest.newBuilder(
                        URI.create("http://127.0.0.1:" + app.port() + path))
                .timeout(Duration.ofSeconds(10));

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

    private HttpResponse<String> send(String method, String path, String body) throws Exception {
        return client.send(
                request(method, path, body, body == null ? null : "application/json"),
                HttpResponse.BodyHandlers.ofString()
        );
    }

    private HttpResponse<String> sendJson(String method, String path, String body)
            throws Exception {
        var response = send(method, path, body);

        assertTrue(
                response.statusCode() < 400,
                () -> method + " " + path
                        + " retornou HTTP " + response.statusCode()
                        + ". Resposta: " + response.body()
        );

        return response;
    }

    private JsonNode readBody(HttpResponse<String> response) throws Exception {
        return json.readTree(response.body());
    }

    @Test
    void fullCrudThroughRealJetty() throws Exception {
        assertEquals(
                "Pet Shop de teste",
                readBody(send("GET", "/", null)).get("application").asText()
        );
        assertEquals(200, send("GET", "/health", null).statusCode());

        var created = sendJson(
                "POST",
                "/servicos",
                "{\"nome\":\" Banho básico \",\"duracaoMinutos\":40}"
        );

        assertEquals(201, created.statusCode());
        assertTrue(created.headers().firstValue("Content-Type")
                .orElseThrow().startsWith("application/json"));
        assertFalse(created.headers().firstValue("X-Request-Id")
                .orElseThrow().isBlank());

        String location = created.headers().firstValue("Location").orElseThrow();
        var createdBody = readBody(created);
        long id = createdBody.get("id").asLong();

        assertTrue(id > 0);
        assertEquals("Banho básico", createdBody.get("nome").asText());
        assertEquals(40, createdBody.get("duracaoMinutos").asInt());
        assertTrue(location.endsWith("/servicos/" + id));

        var fetched = sendJson("GET", location, null);
        assertEquals("Banho básico", readBody(fetched).get("nome").asText());

        assertEquals(
                1,
                readBody(send("GET", "/servicos?nome=BANHO", null)).size()
        );
        assertEquals(
                0,
                readBody(send("GET", "/servicos?nome=tosa", null)).size()
        );

        var updated = sendJson(
                "PUT",
                location,
                "{\"nome\":\"Banho completo\",\"duracaoMinutos\":80}"
        );

        assertEquals(200, updated.statusCode());

        var updatedBody = readBody(updated);
        assertEquals("Banho completo", updatedBody.get("nome").asText());
        assertEquals(80, updatedBody.get("duracaoMinutos").asInt());
        assertEquals(id, updatedBody.get("id").asLong());

        var removed = send("DELETE", location, null);
        assertEquals(204, removed.statusCode());
        assertEquals("", removed.body());

        assertEquals(404, send("GET", location, null).statusCode());
        assertEquals(404, send("DELETE", location, null).statusCode());
        assertEquals(
                404,
                send("PUT", location,
                        "{\"nome\":\"Banho\",\"duracaoMinutos\":20}").statusCode()
        );
    }

    @Test
    void rejectsInvalidInputsWithStructuredErrors() throws Exception {
        var invalid = send(
                "POST",
                "/servicos",
                "{\"nome\":\" \",\"duracaoMinutos\":0}"
        );

        assertEquals(400, invalid.statusCode(), invalid.body());

        var error = readBody(invalid);
        assertEquals(2, error.get("details").size());
        assertEquals(
                invalid.headers().firstValue("X-Request-Id").orElseThrow(),
                error.get("requestId").asText()
        );

        for (String body : List.of(
                "{broken",
                "null",
                "",
                "{}",
                "{\"nome\":\"Banho\",\"duracaoMinutos\":1.5}",
                "{\"nome\":\"Banho\",\"duracaoMinutos\":\"40\"}",
                "{} {}",
                "{\"nome\":\"Banho\",\"duracaoMinutos\":40,\"id\":7}"
        )) {
            var response = send("POST", "/servicos", body);
            assertEquals(400, response.statusCode(), body + " -> " + response.body());
        }

        var invalidId = send("GET", "/servicos/abc", null);
        assertEquals(400, invalidId.statusCode(), invalidId.body());

        var tooLong = send(
                "POST",
                "/servicos",
                "{\"nome\":\"Banho\",\"duracaoMinutos\":241}"
        );
        assertEquals(422, tooLong.statusCode(), tooLong.body());

        var maximum = sendJson(
                "POST",
                "/servicos",
                "{\"nome\":\"Banho especial\",\"duracaoMinutos\":240}"
        );
        assertEquals(201, maximum.statusCode());

        var unsupportedMediaType = client.send(
                request("POST", "/servicos", "{}", "text/plain"),
                HttpResponse.BodyHandlers.ofString()
        );
        assertEquals(415, unsupportedMediaType.statusCode(),
                unsupportedMediaType.body());

        var oversized = send(
                "POST",
                "/servicos",
                "x".repeat(1_048_577)
        );
        assertEquals(413, oversized.statusCode(), oversized.body());
    }

    @Test
    void distinguishesRouteErrorsAndSendsAllowHeader() throws Exception {
        assertEquals(404, send("GET", "/inexistente", null).statusCode());

        var response = send("PATCH", "/servicos/1", "{}");

        assertEquals(405, response.statusCode(), response.body());
        assertEquals(
                "DELETE, GET, PUT",
                response.headers().firstValue("Allow").orElseThrow()
        );
    }

    @Test
    void concurrentRequestsProduceUniqueIds() throws Exception {
        var futures = new ArrayList<CompletableFuture<HttpResponse<String>>>();

        for (int i = 0; i < 30; i++) {
            futures.add(client.sendAsync(
                    request(
                            "POST",
                            "/servicos",
                            "{\"nome\":\"Banho\",\"duracaoMinutos\":10}",
                            "application/json"
                    ),
                    HttpResponse.BodyHandlers.ofString()
            ));
        }

        var ids = new HashSet<Long>();

        for (var future : futures) {
            var response = future.join();

            assertEquals(
                    201,
                    response.statusCode(),
                    "Resposta: " + response.body()
            );

            ids.add(readBody(response).get("id").asLong());
        }

        assertEquals(30, ids.size());
        assertEquals(30, readBody(send("GET", "/servicos", null)).size());
    }
}