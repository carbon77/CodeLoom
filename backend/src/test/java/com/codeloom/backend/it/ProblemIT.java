package com.codeloom.backend.it;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;

@Sql(
    statements = "TRUNCATE TABLE problems CASCADE",
    executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class ProblemIT extends BackendIntegrationTestSupport {
  @Test
  void createsAndFindsProblem() throws Exception {
    mockMvc
        .perform(
            post("/v1/problems")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"Two Sum\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.slug").value("two_sum"));
    mockMvc
        .perform(
            get("/v1/problems/items")
                .param("publishedOnly", "false")
                .principal(TestAuthentication.admin()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].title").value("Two Sum"));
  }

  @Test
  void rejectsMissingTitle() throws Exception {
    mockMvc
        .perform(post("/v1/problems").contentType(MediaType.APPLICATION_JSON).content("{}"))
        .andExpect(status().isBadRequest());
  }
}
