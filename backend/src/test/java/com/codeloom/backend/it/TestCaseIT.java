package com.codeloom.backend.it;

import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.codeloom.backend.dao.problem.ProblemRepository;
import com.codeloom.backend.dao.testcase.TestCaseRepository;
import com.codeloom.backend.model.Problem;
import com.codeloom.backend.model.TestCase;
import java.util.UUID;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;

@Sql(
        statements = {
            "TRUNCATE TABLE test_cases RESTART IDENTITY CASCADE",
            "TRUNCATE TABLE problems RESTART IDENTITY CASCADE"
        },
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class TestCaseIT extends BackendIntegrationTestSupport {
    @Autowired
    TestCaseRepository testCases;

    @Autowired
    ProblemRepository problems;

    private Problem problem() {
        return problems.save(Problem.builder().title("Two Sum").slug("two_sum").build());
    }

    private TestCase testCase(long problemId, boolean isPublic, String input, String output) {
        return testCases.save(TestCase.builder()
                .problemId(problemId)
                .input(input)
                .expectedOutput(output)
                .isPublic(isPublic)
                .build());
    }

    private TestCase testCase(long problemId) {
        return testCase(problemId, true, "1 2", "3");
    }

    @Nested
    class FindOne {
        @Test
        void returnsTestCase() throws Exception {
            var testCase = testCase(problem().getId());
            mockMvc.perform(get("/v1/testCases/{id}", testCase.getId()))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value(testCase.getId().toString()))
                    .andExpect(jsonPath("$.problemId").value(testCase.getProblemId()))
                    .andExpect(jsonPath("$.input").value(testCase.getInput()))
                    .andExpect(jsonPath("$.expectedOutput").value(testCase.getExpectedOutput()))
                    .andExpect(jsonPath("$.isPublic").value(testCase.getIsPublic()));
        }

        @Test
        void missingTestCaseIsNotFound() throws Exception {
            mockMvc.perform(get("/v1/testCases/{id}", UUID.randomUUID())).andExpect(status().isNotFound());
        }
    }

    @Nested
    class FindMany {
        @Test
        void returnsEmptyList() throws Exception {
            mockMvc.perform(get("/v1/testCases/by-ids")
                            .param("ids", UUID.randomUUID().toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(0));
        }

        @Test
        void returnsTestCasesByIds() throws Exception {
            var problem = problem();
            var first = testCase(problem.getId());
            var second = testCase(problem.getId(), true, "2 2", "4");
            mockMvc.perform(get("/v1/testCases/by-ids")
                            .param(
                                    "ids",
                                    first.getId().toString(),
                                    second.getId().toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2));
        }
    }

    @Nested
    class FindByProblemId {
        @Test
        void returnsAllByDefault() throws Exception {
            var problem = problem();
            testCase(problem.getId(), true, "1", "1");
            testCase(problem.getId(), false, "2", "2");
            mockMvc.perform(get("/v1/testCases/by-problem-id/{id}", problem.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2));
        }

        @Test
        void returnsOnlyPublicWhenRequested() throws Exception {
            var problem = problem();
            testCase(problem.getId(), true, "1", "1");
            testCase(problem.getId(), false, "2", "2");
            mockMvc.perform(get("/v1/testCases/by-problem-id/{id}", problem.getId())
                            .param("isPublic", "true"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1));
        }
    }

    @Nested
    class Create {
        @Test
        void createsTestCase() throws Exception {
            var problem = problem();
            var body = """
                    {"problemId":%d,"input":"1 2","expectedOutput":"3","isPublic":true}
                    """.formatted(problem.getId());
            mockMvc.perform(post("/v1/testCases")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", notNullValue()))
                    .andExpect(jsonPath("$.problemId").value(problem.getId()))
                    .andExpect(jsonPath("$.input").value("1 2"))
                    .andExpect(jsonPath("$.expectedOutput").value("3"))
                    .andExpect(jsonPath("$.isPublic").value(true));
            assertEquals(1, testCases.count());
        }

        @Test
        void malformedBodyIsBadRequest() throws Exception {
            mockMvc.perform(post("/v1/testCases")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.path").value("/v1/testCases"));
        }

        @Test
        void validationErrorsAreReturned() throws Exception {
            var body = """
                    {"problemId":123,"input":"","expectedOutput":"","isPublic":true}
                    """;
            mockMvc.perform(post("/v1/testCases")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.path").value("/v1/testCases"))
                    .andExpect(jsonPath("$.payload", notNullValue()));
        }
    }

    @Nested
    class Update {
        @Test
        void updatesTestCase() throws Exception {
            var firstProblem = problem();
            var secondProblem =
                    problems.save(Problem.builder().title("Sort").slug("sort").build());
            var testCase = testCase(firstProblem.getId(), false, "1 2", "3");
            mockMvc.perform(put("/v1/testCases/{id}", testCase.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"problemId":%d,"input":"2 2","expectedOutput":"4","isPublic":true}
                                    """.formatted(secondProblem.getId())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(testCase.getId().toString()))
                    .andExpect(jsonPath("$.problemId").value(secondProblem.getId()))
                    .andExpect(jsonPath("$.input").value("2 2"))
                    .andExpect(jsonPath("$.expectedOutput").value("4"))
                    .andExpect(jsonPath("$.isPublic").value(true));
            var updated = testCases.findById(testCase.getId()).orElseThrow();
            assertTrue(updated.getIsPublic());
            assertEquals(secondProblem.getId(), updated.getProblemId());
        }

        @Test
        void rejectsPartialUpdate() throws Exception {
            var testCase = testCase(problem().getId());
            mockMvc.perform(put("/v1/testCases/{id}", testCase.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"isPublic\":false}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400));
        }
    }

    @Nested
    class Delete {
        @Test
        void deletesTestCase() throws Exception {
            var testCase = testCase(problem().getId());
            mockMvc.perform(delete("/v1/testCases/{id}", testCase.getId())).andExpect(status().isOk());
            assertEquals(0, testCases.count());
        }
    }
}
