package com.codeloom.backend.it;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;

@Sql(statements = "TRUNCATE TABLE topics CASCADE", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class TopicIT extends BackendIntegrationTestSupport {
    @Test
    void createsAndListsTopic() throws Exception {
        mockMvc.perform(post("/v1/topics")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"graphs\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("graphs"));
        mockMvc.perform(get("/v1/topics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("graphs"));
    }

    @Test
    void rejectsBlankName() throws Exception {
        mockMvc.perform(post("/v1/topics")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\"}"))
                .andExpect(status().isBadRequest());
    }
}
