
package br.com.minispring;

import org.junit.jupiter.api.Test;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(
        classes = {
                br.com.minispring.exemplo.Application.class,
                RouterTest.TestRoutesConfig.class
        }
)
@AutoConfigureMockMvc
class RouterTest {

    @Autowired
    private MockMvc mockMvc;

    @TestConfiguration
    static class TestRoutesConfig {

        @Bean
        Routes routes() {
            return new Routes();
        }
    }

    @RestController
    @RequestMapping("/items")
    static class Routes {

        @GetMapping("/{id}")
        public String item(@PathVariable("id") long id) {
            return "item";
        }

        @GetMapping("/resumo")
        public String summary() {
            return "summary";
        }
    }

    @Test
    void literalRouteHasPriority() throws Exception {

        mockMvc.perform(get("/items/resumo"))
                .andExpect(status().isOk())
                .andExpect(content().string("summary"));

        mockMvc.perform(get("/items/42"))
                .andExpect(status().isOk())
                .andExpect(content().string("item"));
    }

    @Test
    void distinguishesNotFoundAndMethodNotAllowed()
            throws Exception {

        mockMvc.perform(get("/unknown"))
                .andExpect(status().isNotFound());

        mockMvc.perform(
                        post("/items/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{}")
                )
                .andExpect(status().isMethodNotAllowed());
    }
}
