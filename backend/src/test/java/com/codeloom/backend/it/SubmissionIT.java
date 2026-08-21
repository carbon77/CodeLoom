package com.codeloom.backend.it;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.codeloom.backend.dao.problem.ProblemRepository;
import com.codeloom.backend.dao.testcase.TestCaseRepository;
import com.codeloom.backend.model.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;

@Sql(statements = "TRUNCATE TABLE problems CASCADE", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class SubmissionIT extends BackendIntegrationTestSupport {
    @Autowired
    ProblemRepository problems;

    @Autowired
    TestCaseRepository tests;

    @MockitoBean
    KafkaTemplate<String, String> kafka;

    @Test
    void sendsAndPersistsSubmission() throws Exception {
        Problem p = problems.save(new Problem("Sum", "sum"));
        tests.save(new TestCase(p.getId(), "1 2", "3", true));
        String body = "{\"problemId\":" + p.getId() + ",\"code\":\"print(3)\",\"language\":\"python\"}";
        mockMvc.perform(post("/v1/submissions")
                        .principal(TestAuthentication.admin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());
        verify(kafka).send(anyString(), anyString(), contains("print(3)"));
        mockMvc.perform(get("/v1/submissions")
                        .principal(TestAuthentication.admin())
                        .param("problemId", p.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("PENDING"));
    }

    @Test
    void rejectsUnknownLanguage() throws Exception {
        mockMvc.perform(post("/v1/submissions")
                        .principal(TestAuthentication.admin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"problemId\":1,\"code\":\"x\",\"language\":\"ruby\"}"))
                .andExpect(status().isBadRequest());
    }
}
