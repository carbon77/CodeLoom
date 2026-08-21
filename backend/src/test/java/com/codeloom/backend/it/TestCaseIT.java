package com.codeloom.backend.it;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.codeloom.backend.dao.problem.ProblemRepository;
import com.codeloom.backend.model.Problem;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;

@Sql(statements = "TRUNCATE TABLE problems CASCADE", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class TestCaseIT extends BackendIntegrationTestSupport {
    @Autowired
    ProblemRepository problems;

    @Test
    void createsAndFindsTestCase() throws Exception {
        Problem p = problems.save(new Problem("Sum", "sum"));
        String body = "{\"problemId\":" + p.getId() + ",\"input\":\"1 2\",\"expectedOutput\":\"3\",\"isPublic\":true}";
        mockMvc.perform(post("/v1/testCases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.expectedOutput").value("3"));
        mockMvc.perform(get("/v1/testCases/by-problem-id/" + p.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].input").value("1 2"));
    }
}
