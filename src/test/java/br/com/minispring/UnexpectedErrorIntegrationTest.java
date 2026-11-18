
package br.com.minispring;

import br.com.minispring.exemplo.Application;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;

import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(
        classes = {
                Application.class,
                UnexpectedErrorIntegrationTest.FailureConfig.class
        }
)
@AutoConfigureMockMvc
class UnexpectedErrorIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper json = new ObjectMapper();

    @TestConfiguration
    static class FailureConfig {

        @Bean
        FailureController failureController() {
            return new FailureController();
        }
    }

    @RestController
    static class FailureController {

        @GetMapping("/failure")
        public String failure() {
            throw new IllegalStateException(
                    "detalhe-interno-do-teste"
            );
        }
    }

    @Test
    void unexpectedFailureReturnsJsonWithoutLeakingImplementationDetails()
            throws Exception {

        var result = mockMvc.perform(
                        get("/failure")
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isInternalServerError())
                .andReturn();

        String responseBody = result.getResponse()
                .getContentAsString();

        JsonNode body = json.readTree(responseBody);

        assertEquals(
                "Erro interno do servidor",
                body.get("message").asText()
        );

        assertFalse(
                responseBody.contains("detalhe-interno-do-teste")
        );

        String requestId = result.getResponse()
                .getHeader("X-Request-Id");

        assertNotNull(requestId);
        assertFalse(requestId.isBlank());

        assertEquals(
                requestId,
                body.get("requestId").asText()
        );
    }
}
