package com.codeloom.backend.it;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.codeloom.backend.dao.SubmissionRepository;
import com.codeloom.backend.dao.problem.ProblemRepository;
import com.codeloom.backend.dao.testcase.TestCaseRepository;
import com.codeloom.backend.model.Problem;
import com.codeloom.backend.model.ProblemDifficulty;
import com.codeloom.backend.model.Submission;
import com.codeloom.backend.model.TestCase;
import com.codeloom.backend.security.UserRole;
import com.codeloom.common.SubmissionEvent;
import com.codeloom.common.SubmissionStatus;
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
                    .andExpect(jsonPath("$[0].id").value(own.getId().toString()))
                    .andExpect(jsonPath("$[0].userId").value(TEST_USER_ID.toString()))
                    .andExpect(jsonPath("$[0].problemId").value(problem.getId()))
                    .andExpect(jsonPath("$[0].code").value("own code"));
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
    class SendSubmission {
        @Test
        void userCreatesPendingSubmissionAndPublishesEvent() throws Exception {
            var problem = problem(true, "Two Sum", "two_sum", true);
            mockMvc.perform(post("/v1/submissions")
                            .principal(user(UserRole.USER))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(request(problem.getId())))
                    .andExpect(status().isOk());

            var submission = submissions.findAll().iterator().next();
            assertEquals(TEST_USER_ID, submission.getUserId());
            assertEquals(problem.getId().longValue(), submission.getProblemId());
            assertEquals("println(42)", submission.getCode());
            assertEquals("java", submission.getLanguage());
            assertEquals(SubmissionStatus.PENDING, submission.getStatus());

            var value = ArgumentCaptor.forClass(String.class);
            verify(kafka).send(eq("test-submissions"), eq(submission.getId().toString()), value.capture());
            var event = mapper.readValue(value.getValue(), SubmissionEvent.class);
            assertEquals(submission.getId(), event.submissionId());
            assertEquals(TEST_USER_ID, event.userId());
            assertEquals(problem.getId().longValue(), event.problemId());
            assertEquals("println(42)", event.code());
            assertEquals("java", event.language());
        }

        @Test
        void adminCanSubmitToUnpublishedProblem() throws Exception {
            var problem = problem(false, "Two Sum", "two_sum", true);
            mockMvc.perform(post("/v1/submissions")
                            .principal(admin())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(request(problem.getId())))
                    .andExpect(status().isOk());
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
        return submissions.save(new Submission(userId, problemId, code, SubmissionStatus.PENDING, "java"));
    }

    private String request(long problemId) {
        return "{\"problemId\":" + problemId + ",\"code\":\"println(42)\",\"language\":\"java\"}";
    }
}
