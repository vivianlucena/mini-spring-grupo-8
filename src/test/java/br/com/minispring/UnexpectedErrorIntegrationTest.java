package br.com.minispring;

import br.com.minispring.framework.MiniApplication;
import br.com.minispring.framework.context.AppConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

class UnexpectedErrorIntegrationTest {

    @Test
    void unexpectedFailureReturnsJsonWithoutLeakingImplementationDetails() throws Exception {
        var config = new AppConfig(new Properties(), Map.of());

        try (
                var app = MiniApplication.start(
                        "br.com.minispring.failurefixture",
                        config,
                        0
                );
                var client = HttpClient.newHttpClient()
        ) {
            var request = HttpRequest.newBuilder(
                            URI.create("http://127.0.0.1:" + app.port() + "/failure")
                    )
                    .timeout(Duration.ofSeconds(5))
                    .GET()
                    .build();

            var response = client.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            assertEquals(500, response.statusCode());

            var body = new ObjectMapper().readTree(response.body());

            assertEquals(
                    "Erro interno do servidor",
                    body.get("message").asText()
            );

            assertFalse(response.body().contains("detalhe-interno-do-teste"));

            assertEquals(
                    response.headers().firstValue("X-Request-Id").orElseThrow(),
                    body.get("requestId").asText()
            );
        }
    }
}