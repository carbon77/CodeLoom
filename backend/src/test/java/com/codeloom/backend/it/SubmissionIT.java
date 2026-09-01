package com.codeloom.backend.it;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.codeloom.backend.dao.problem.ProblemRepository;
import com.codeloom.backend.dao.submission.SubmissionRepository;
import com.codeloom.backend.dao.testcase.TestCaseRepository;
import com.codeloom.backend.dao.testcase.TestCaseResultRepository;
import com.codeloom.backend.model.Problem;
import com.codeloom.backend.model.ProblemConstraints;
import com.codeloom.backend.model.ProblemDifficulty;
import com.codeloom.backend.model.Submission;
import com.codeloom.backend.model.TestCase;
import com.codeloom.backend.model.TestCaseResult;
import com.codeloom.backend.security.UserRole;
import com.codeloom.common.SubmissionKafkaEvent;
import com.codeloom.common.SubmissionState;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import tools.jackson.databind.ObjectMapper;

@Sql(
        statements = {
            "TRUNCATE TABLE test_case_results CASCADE",
            "TRUNCATE TABLE submissions CASCADE",
            "TRUNCATE TABLE problems RESTART IDENTITY CASCADE"
        },
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class SubmissionIT extends BackendIntegrationTestSupport {
    @Autowired
    ProblemRepository problems;

    @Autowired
    SubmissionRepository submissions;

    @Autowired
    TestCaseRepository testCases;

    @Autowired
    TestCaseResultRepository testCaseResults;

    @Autowired
    ObjectMapper mapper;

    @MockitoBean
    KafkaTemplate<String, String> kafka;

    @Nested
    class FindSubmissions {
        @Test
        void returnsOnlyAuthenticatedUsersSubmissionsForProblem() throws Exception {
            var problem = problem(true, "Two Sum", "two_sum", true);
            var own = submission(problem.getId(), TEST_USER_ID, "own code");
            submission(problem.getId(), UUID.randomUUID(), "other code");
            var otherProblem = problem(true, "Sort", "sort", true);
            submission(otherProblem.getId(), TEST_USER_ID, "other problem code");

            mockMvc.perform(get("/v1/submissions")
                            .principal(admin())
                            .param("problemId", problem.getId().toString()))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].submissionId").value(own.getId().toString()))
                    .andExpect(jsonPath("$[0].state").value("PENDING"))
                    .andExpect(jsonPath("$[0].language").value("java"))
                    .andExpect(jsonPath("$[0].createdAt", notNullValue()))
                    .andExpect(jsonPath("$[0].userId").doesNotExist())
                    .andExpect(jsonPath("$[0].code").doesNotExist());
        }

        @Test
        void returnsEmptyWhenUserHasNoSubmissions() throws Exception {
            var problem = problem(true, "Two Sum", "two_sum", true);
            mockMvc.perform(get("/v1/submissions")
                            .principal(admin())
                            .param("problemId", problem.getId().toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(0));
        }
    }

    @Nested
    class FindSubmissionDetails {
        @Test
        void returnsOwnedSubmissionWithResults() throws Exception {
            var problem = problem(true, "Two Sum", "two_sum", true);
            var submission = submissions.save(Submission.builder()
                    .userId(TEST_USER_ID)
                    .problemId(problem.getId())
                    .code("println(42)")
                    .state(SubmissionState.ACCEPTED)
                    .language("java")
                    .errorMessage(null)
                    .build());
            testCaseResults.save(TestCaseResult.builder()
                    .submissionId(submission.getId())
                    .input("1 2")
                    .expectedOutput("3")
                    .stdout("3")
                    .stderr("")
                    .executionTimeMs(12L)
                    .bytesUsed(1024L)
                    .build());

            mockMvc.perform(get("/v1/submissions/{submissionId}", submission.getId())
                            .principal(admin()))
                    .andExpect(status().isOk())
                    .andExpect(
                            jsonPath("$.submissionId").value(submission.getId().toString()))
                    .andExpect(jsonPath("$.state").value("ACCEPTED"))
                    .andExpect(jsonPath("$.language").value("java"))
                    .andExpect(jsonPath("$.code").value("println(42)"))
                    .andExpect(jsonPath("$.errorMessage").value(nullValue()))
                    .andExpect(jsonPath("$.createdAt", notNullValue()))
                    .andExpect(jsonPath("$.results.length()").value(1))
                    .andExpect(jsonPath("$.results[0].input").value("1 2"))
                    .andExpect(jsonPath("$.results[0].expectedOutput").value("3"))
                    .andExpect(jsonPath("$.results[0].stdout").value("3"));
        }

        @Test
        void returnsNotFoundForAnotherUsersSubmission() throws Exception {
            var problem = problem(true, "Two Sum", "two_sum", true);
            var submission = submission(problem.getId(), UUID.randomUUID(), "other code");

            mockMvc.perform(get("/v1/submissions/{submissionId}", submission.getId())
                            .principal(admin()))
                    .andExpect(status().isNotFound());
        }

        @Test
        void returnsNotFoundForMissingSubmission() throws Exception {
            mockMvc.perform(get("/v1/submissions/{submissionId}", UUID.randomUUID())
                            .principal(admin()))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    class SendSubmission {
        @Test
        void userCreatesPendingSubmissionAndPublishesEvent() throws Exception {
            var problem = problem(true, "Two Sum", "two_sum", true);
            mockMvc.perform(post("/v1/submissions")
                            .principal(user(UserRole.USER))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(request(problem.getId())))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.state").value("PENDING"))
                    .andExpect(jsonPath("$.submissionId").exists());

            var submission = submissions.findAll().iterator().next();
            assertEquals(TEST_USER_ID, submission.getUserId());
            assertEquals(problem.getId().longValue(), submission.getProblemId());
            assertEquals("println(42)", submission.getCode());
            assertEquals("java", submission.getLanguage());
            assertEquals(SubmissionState.PENDING, submission.getState());

            var value = ArgumentCaptor.forClass(String.class);
            verify(kafka).send(eq("test-submissions"), eq(submission.getId().toString()), value.capture());
            var event = mapper.readValue(value.getValue(), SubmissionKafkaEvent.class);
            assertEquals(submission.getId(), event.submissionId());
            assertEquals(TEST_USER_ID, event.userId());
            assertEquals(problem.getId().longValue(), event.problemId());
            assertEquals("println(42)", event.code());
            assertEquals("java", event.language());
            assertEquals(2000L, event.executionTimeLimitMs());
            assertEquals(64L, event.memoryUsageLimitMb());
        }

        @Test
        void adminCanSubmitToUnpublishedProblem() throws Exception {
            var problem = problem(false, "Two Sum", "two_sum", true);
            mockMvc.perform(post("/v1/submissions")
                            .principal(admin())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(request(problem.getId())))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.state").value("PENDING"))
                    .andExpect(jsonPath("$.submissionId").exists());
            assertEquals(1, submissions.count());
            verify(kafka).send(eq("test-submissions"), anyString(), anyString());
        }

        @Test
        void userCannotSubmitToUnpublishedProblem() throws Exception {
            var problem = problem(false, "Two Sum", "two_sum", true);
            mockMvc.perform(post("/v1/submissions")
                            .principal(user(UserRole.USER))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(request(problem.getId())))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404));
            assertEquals(0, submissions.count());
            verify(kafka, never()).send(eq("test-submissions"), anyString(), anyString());
        }

        @Test
        void missingProblemIsNotFound() throws Exception {
            mockMvc.perform(post("/v1/submissions")
                            .principal(user(UserRole.USER))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(request(999)))
                    .andExpect(status().isNotFound());
            assertEquals(0, submissions.count());
            verify(kafka, never()).send(eq("test-submissions"), anyString(), anyString());
        }

        @Test
        void rejectsProblemWithoutTestCases() throws Exception {
            var problem = problem(true, "Two Sum", "two_sum", false);
            mockMvc.perform(post("/v1/submissions")
                            .principal(user(UserRole.USER))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(request(problem.getId())))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath(
                            "$.message",
                            containsString("Problem id=" + problem.getId() + " does not have any test cases")))
                    .andExpect(jsonPath("$.path").value("/v1/submissions"));
            assertEquals(0, submissions.count());
            verify(kafka, never()).send(eq("test-submissions"), anyString(), anyString());
        }

        @Test
        void rejectsBlankCodeAndLanguage() throws Exception {
            var problem = problem(true, "Two Sum", "two_sum", true);
            var body = "{\"problemId\":" + problem.getId() + ",\"code\":\"\",\"language\":\"\"}";
            mockMvc.perform(post("/v1/submissions")
                            .principal(user(UserRole.USER))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.payload.code", notNullValue()))
                    .andExpect(jsonPath("$.payload.language", notNullValue()));
            assertEquals(0, submissions.count());
            verify(kafka, never()).send(eq("test-submissions"), anyString(), anyString());
        }

        @Test
        void rejectsUnsupportedLanguage() throws Exception {
            var problem = problem(true, "Two Sum", "two_sum", true);
            var body = "{\"problemId\":" + problem.getId() + ",\"code\":\"puts 42\",\"language\":\"unknown\"}";
            mockMvc.perform(post("/v1/submissions")
                            .principal(user(UserRole.USER))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.payload.language").value("Invalid submission language"));
            assertEquals(0, submissions.count());
            verify(kafka, never()).send(eq("test-submissions"), anyString(), anyString());
        }
    }

    private Problem problem(boolean published, String title, String slug, boolean withTestCase) {
        var problem = problems.save(Problem.builder()
                .title(title)
                .slug(slug)
                .description("")
                .difficulty(ProblemDifficulty.EASY)
                .constraints(new ProblemConstraints(2000L, 64L))
                .hints(List.of())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .publishedAt(published ? Instant.now() : null)
                .build());
        if (withTestCase) {
            testCases.save(TestCase.builder()
                    .problemId(problem.getId())
                    .input("1 2")
                    .expectedOutput("3")
                    .build());
        }
        return problem;
    }

    private Submission submission(long problemId, UUID userId, String code) {
        return submissions.save(Submission.builder()
                .userId(userId)
                .problemId(problemId)
                .code(code)
                .state(SubmissionState.PENDING)
                .language("java")
                .build());
    }

    private String request(long problemId) {
        return "{\"problemId\":" + problemId + ",\"code\":\"println(42)\",\"language\":\"java\"}";
    }
}
