package com.codeloom.backend.it;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.codeloom.backend.dao.problem.ProblemRepository;
import com.codeloom.backend.dao.testcase.TestCaseRepository;
import com.codeloom.backend.dao.topic.TopicRepository;
import com.codeloom.backend.model.*;
import com.codeloom.backend.security.UserRole;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.ResultActions;

@Sql(
        statements = {
            "TRUNCATE TABLE problem_topics CASCADE",
            "TRUNCATE TABLE topics CASCADE",
            "TRUNCATE TABLE problems RESTART IDENTITY CASCADE"
        },
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class ProblemIT extends BackendIntegrationTestSupport {
    @Autowired
    TopicRepository topicRepository;

    @Autowired
    ProblemRepository problemRepository;

    @Autowired
    TestCaseRepository testCaseRepository;

    List<Topic> seededTopics;
    List<Problem> seededProblems;

    @Nested
    class FindAllItems {
        @Test
        void empty() throws Exception {
            mockMvc.perform(get("/v1/problems/items").principal(admin()))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.length()").value(0));
        }

        @Test
        void returnsProblems() throws Exception {
            seed();
            mockMvc.perform(get("/v1/problems/items").principal(admin()).param("publishedOnly", "false"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.length()").value(3))
                    .andExpect(jsonPath("$[0].problemId").value(1))
                    .andExpect(jsonPath("$[0].title").value("Two Sum"))
                    .andExpect(jsonPath("$[0].slug").value("two_sum"))
                    .andExpect(jsonPath("$[0].difficulty").value("EASY"))
                    .andExpect(jsonPath("$[0].publishedAt").value(nullValue()))
                    .andExpect(jsonPath("$[1].title").value("Sort"))
                    .andExpect(jsonPath("$[1].slug").value("sort"))
                    .andExpect(jsonPath("$[1].difficulty").value("MEDIUM"))
                    .andExpect(jsonPath("$[1].publishedAt", notNullValue()))
                    .andExpect(jsonPath("$[2].title").value("B-Tree Sort"))
                    .andExpect(jsonPath("$[2].slug").value("b-tree_sort"))
                    .andExpect(jsonPath("$[2].difficulty").value("HARD"))
                    .andExpect(jsonPath("$[2].publishedAt", notNullValue()));
        }

        @Test
        void userSeesOnlyPublishedProblems() throws Exception {
            seed();
            mockMvc.perform(get("/v1/problems/items").principal(user(UserRole.USER)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[0].slug").value("sort"))
                    .andExpect(jsonPath("$[1].slug").value("b-tree_sort"));
        }

        @Test
        void userCannotRequestUnpublishedProblems() throws Exception {
            mockMvc.perform(get("/v1/problems/items")
                            .principal(user(UserRole.USER))
                            .param("publishedOnly", "false"))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.status").value(403));
        }
    }

    @Nested
    class FindBySlug {
        @Test
        void returnsProblem() throws Exception {
            seed();
            checkProblem(
                    mockMvc.perform(get("/v1/problems/slug/two_sum").principal(admin()))
                            .andExpect(status().isOk())
                            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                            .andExpect(jsonPath("$.id").value(1)),
                    seededProblems.getFirst(),
                    false);
        }

        @Test
        void missingSlugIsNotFound() throws Exception {
            mockMvc.perform(get("/v1/problems/slug/1j2hn").principal(admin())).andExpect(status().isNotFound());
        }

        @Test
        void userCannotSeeUnpublishedProblem() throws Exception {
            seed();
            mockMvc.perform(get("/v1/problems/slug/two_sum").principal(user(UserRole.USER)))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    class FindById {
        @Test
        void returnsProblem() throws Exception {
            seed();
            checkProblem(
                    mockMvc.perform(get("/v1/problems/1").principal(admin()))
                            .andExpect(status().isOk())
                            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                            .andExpect(jsonPath("$.id").value(1)),
                    seededProblems.getFirst(),
                    true);
        }

        @Test
        void missingIdIsNotFound() throws Exception {
            mockMvc.perform(get("/v1/problems/3123").principal(admin())).andExpect(status().isNotFound());
        }

        @Test
        void userCannotSeeUnpublishedProblem() throws Exception {
            seed();
            mockMvc.perform(get("/v1/problems/1").principal(user(UserRole.USER)))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    class DeleteProblem {
        @Test
        void deletesProblem() throws Exception {
            seed();
            mockMvc.perform(delete("/v1/problems/1").principal(admin())).andExpect(status().isOk());
            assertEquals(2, problemRepository.count());
        }
    }

    @Nested
    class Create {
        @Test
        void createsProblem() throws Exception {
            var expectedProblem = Problem.builder()
                    .title("Merge two Arrays")
                    .slug("merge_two_arrays")
                    .build();
            checkProblem(
                    mockMvc.perform(post("/v1/problems")
                                    .principal(admin())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content("{\"title\":\"Merge two Arrays\"}"))
                            .andExpect(status().isOk())
                            .andExpect(jsonPath("$.id").value(1)),
                    expectedProblem,
                    true);
            assertEquals(1, problemRepository.count());
        }

        @Test
        void duplicateSlugIsBadRequest() throws Exception {
            var request = post("/v1/problems")
                    .principal(admin())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"title\":\"Merge two Arrays\"}");
            mockMvc.perform(request).andExpect(status().isOk());
            mockMvc.perform(post("/v1/problems")
                            .principal(admin())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"title\":\"Merge two Arrays\"}"))
                    .andExpect(status().isBadRequest());
            assertEquals(1, problemRepository.count());
        }
    }

    @Nested
    class Update {
        @Test
        void updatesAllProblemFields() throws Exception {
            seed();
            var before = problemRepository.findById(1L).orElseThrow();
            var body = """
                    {
                      "title":"Three Sum","slug":"three_sum","difficulty":"HARD",
                      "description":"Find three numbers that sum to zero",
                      "hints":["hint #1","hint #2","hint #3"],
                      "examples":{"examples":[{"input":"nums = [-1,0,1,2,-1,-4]",
                        "output":"[[-1,-1,2],[-1,0,1]]","explanation":"These triplets sum to zero"}]},
                      "constraints":{"executionTimeLimitMs":3000,"memoryUsageLimitBytes":4},
                      "topics":[]
                    }
                    """;
            mockMvc.perform(put("/v1/problems/1")
                            .principal(admin())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.title").value("Three Sum"))
                    .andExpect(jsonPath("$.slug").value("three_sum"))
                    .andExpect(jsonPath("$.difficulty").value("HARD"))
                    .andExpect(jsonPath("$.description").value("Find three numbers that sum to zero"))
                    .andExpect(jsonPath("$.hints.length()").value(3))
                    .andExpect(jsonPath("$.hints[0]").value("hint #1"))
                    .andExpect(jsonPath("$.hints[1]").value("hint #2"))
                    .andExpect(jsonPath("$.hints[2]").value("hint #3"))
                    .andExpect(jsonPath("$.examples.examples.length()").value(1))
                    .andExpect(jsonPath("$.examples.examples[0].input").value("nums = [-1,0,1,2,-1,-4]"))
                    .andExpect(jsonPath("$.examples.examples[0].output").value("[[-1,-1,2],[-1,0,1]]"))
                    .andExpect(jsonPath("$.examples.examples[0].explanation").value("These triplets sum to zero"))
                    .andExpect(jsonPath("$.constraints.executionTimeLimitMs").value(3000))
                    .andExpect(jsonPath("$.constraints.memoryUsageLimitBytes").value(4))
                    .andExpect(jsonPath("$.createdAt", notNullValue()))
                    .andExpect(jsonPath("$.updatedAt", notNullValue()));

            var after = problemRepository.findById(1L).orElseThrow();
            assertEquals("Three Sum", after.getTitle());
            assertEquals("three_sum", after.getSlug());
            assertEquals(ProblemDifficulty.HARD, after.getDifficulty());
            assertEquals("Find three numbers that sum to zero", after.getDescription());
            assertEquals(3, after.getHints().size());
            assertEquals(before.getCreatedAt(), after.getCreatedAt());
            assertEquals(before.getPublishedAt(), after.getPublishedAt());
            assertNotEquals(before.getUpdatedAt(), after.getUpdatedAt());
            assertEquals(
                    0,
                    StreamSupport.stream(topicRepository.findByProblemId(1).spliterator(), false)
                            .count());
        }

        @Test
        void missingIdIsNotFound() throws Exception {
            mockMvc.perform(put("/v1/problems/999")
                            .principal(admin())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(fullProblemBody("Updated Title", "updated_slug", "[]")))
                    .andExpect(status().isNotFound());
        }

        @Test
        void duplicateSlugIsBadRequest() throws Exception {
            seed();
            mockMvc.perform(put("/v1/problems/1")
                            .principal(admin())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(fullProblemBody("Two Sum", "sort", "[]")))
                    .andExpect(status().isBadRequest());
            assertEquals("two_sum", problemRepository.findById(1L).orElseThrow().getSlug());
            assertEquals("sort", problemRepository.findById(2L).orElseThrow().getSlug());
            assertEquals(3, problemRepository.count());
        }

        @Test
        void rejectsPartialUpdate() throws Exception {
            seed();
            mockMvc.perform(put("/v1/problems/1")
                            .principal(admin())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"title\":\"Updated Two Sum\",\"difficulty\":\"EASY\"}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400));
            assertEquals("Two Sum", problemRepository.findById(1L).orElseThrow().getTitle());
        }

        @Test
        void updatesTopicsAndCreatesMissingTopics() throws Exception {
            seed();
            assertEquals(3, topicRepository.count());
            var topics = """
                    [
                      {"topic_id":"incorrect_uuid"},{"topic_id":"%s"},
                      {"name":"Two Pointers"},{"name":"Hash Table"}]
                    """.formatted(seededTopics.getFirst().getId());
            mockMvc.perform(put("/v1/problems/1")
                            .principal(admin())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(fullProblemBody("Three Sum", "three_sum", topics)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.title").value("Three Sum"))
                    .andExpect(jsonPath("$.slug").value("three_sum"));

            assertEquals(5, topicRepository.count());
            assertEquals(
                    Set.of("topic1", "topic2", "topic3", "Two Pointers", "Hash Table"),
                    names(topicRepository.findAll()));
            var linked = StreamSupport.stream(topicRepository.findByProblemId(1).spliterator(), false)
                    .toList();
            assertEquals(3, linked.size());
            assertEquals(Set.of("topic1", "Two Pointers", "Hash Table"), names(linked));
        }
    }

    @Nested
    class Publication {
        @Test
        void publishesProblem() throws Exception {
            seed();
            var before = problemRepository.findById(1L).orElseThrow();
            testCaseRepository.save(TestCase.builder()
                    .problemId(before.getId())
                    .input("1 2")
                    .expectedOutput("3")
                    .build());
            assertNull(before.getPublishedAt());
            assertEquals(before.getCreatedAt(), before.getUpdatedAt());
            mockMvc.perform(patch("/v1/problems/1/publish").principal(admin())).andExpect(status().isOk());
            var after = problemRepository.findById(1L).orElseThrow();
            assertNotNull(after.getPublishedAt());
            assertNotEquals(after.getCreatedAt(), after.getUpdatedAt());
        }

        @Test
        void rejectsProblemWithoutTestCases() throws Exception {
            seed();
            assertNull(problemRepository.findById(1L).orElseThrow().getPublishedAt());
            mockMvc.perform(patch("/v1/problems/1/publish").principal(admin()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.message", containsString("Problem id=1 does not have any test cases")))
                    .andExpect(jsonPath("$.path").value("/v1/problems/1/publish"));
            assertNull(problemRepository.findById(1L).orElseThrow().getPublishedAt());
        }

        @Test
        void unpublishesProblem() throws Exception {
            seed();
            var before = problemRepository.findById(2L).orElseThrow();
            assertNotNull(before.getPublishedAt());
            assertEquals(before.getCreatedAt(), before.getUpdatedAt());
            mockMvc.perform(patch("/v1/problems/2/unpublish").principal(admin()))
                    .andExpect(status().isOk());
            var after = problemRepository.findById(2L).orElseThrow();
            assertNull(after.getPublishedAt());
            assertNotEquals(after.getCreatedAt(), after.getUpdatedAt());
        }
    }

    private void seed() {
        var t1 = topicRepository.save(Topic.builder().name("topic1").build());
        var t2 = topicRepository.save(Topic.builder().name("topic2").build());
        var t3 = topicRepository.save(Topic.builder().name("topic3").build());
        var now = Instant.now();
        var p1 = problemRepository.save(Problem.builder()
                .title("Two Sum")
                .slug("two_sum")
                .description("Find two numbers")
                .difficulty(ProblemDifficulty.EASY)
                .hints(List.of("hint #1", "hint #2"))
                .examples(new ProblemExamples(List.of(new ProblemExample("input", "output", "explain"))))
                .constraints(new ProblemConstraints(2000L, 4L))
                .createdAt(now)
                .updatedAt(now)
                .build());
        var p2 = problemRepository.save(problem("Sort", "sort", ProblemDifficulty.MEDIUM, now));
        var p3 = problemRepository.save(problem("B-Tree Sort", "b-tree_sort", ProblemDifficulty.HARD, now));
        seededTopics = List.of(t1, t2, t3);
        seededProblems = List.of(p1, p2, p3);
        topicRepository.saveAllProblemRelationships(List.of(
                new ProblemTopicRelationship(t3.getId(), p1.getId()),
                new ProblemTopicRelationship(t1.getId(), p2.getId()),
                new ProblemTopicRelationship(t2.getId(), p2.getId()),
                new ProblemTopicRelationship(t3.getId(), p2.getId()),
                new ProblemTopicRelationship(t2.getId(), p3.getId()),
                new ProblemTopicRelationship(t3.getId(), p3.getId())));
    }

    private Problem problem(String title, String slug, ProblemDifficulty difficulty, Instant now) {
        return Problem.builder()
                .title(title)
                .slug(slug)
                .description("")
                .difficulty(difficulty)
                .hints(List.of())
                .createdAt(now)
                .updatedAt(now)
                .publishedAt(Instant.now())
                .build();
    }

    private Set<String> names(Iterable<Topic> topics) {
        return StreamSupport.stream(topics.spliterator(), false)
                .map(Topic::getName)
                .collect(Collectors.toSet());
    }

    private String fullProblemBody(String title, String slug, String topics) {
        return """
                {
                  "title":"%s","slug":"%s","description":"Full description",
                  "difficulty":"MEDIUM",
                  "constraints":{"executionTimeLimitMs":2000,"memoryUsageLimitBytes":4},
                  "examples":{"examples":[]},"hints":[],"topics":%s
                }
                """.formatted(title, slug, topics);
    }

    private void checkProblem(ResultActions result, Problem problem, boolean timestamps) throws Exception {
        result.andExpect(jsonPath("$.title").value(problem.getTitle()))
                .andExpect(jsonPath("$.slug").value(problem.getSlug()))
                .andExpect(
                        jsonPath("$.difficulty").value(problem.getDifficulty().name()))
                .andExpect(jsonPath("$.description").value(problem.getDescription()))
                .andExpect(jsonPath("$.hints.length()").value(problem.getHints().size()));
        for (int i = 0; i < problem.getHints().size(); i++) {
            result.andExpect(
                    jsonPath("$.hints[" + i + "]").value(problem.getHints().get(i)));
        }
        if (problem.getExamples() != null) {
            result.andExpect(jsonPath("$.examples.examples.length()")
                    .value(problem.getExamples().examples().size()));
            for (int i = 0; i < problem.getExamples().examples().size(); i++) {
                var example = problem.getExamples().examples().get(i);
                result.andExpect(
                                jsonPath("$.examples.examples[" + i + "].input").value(example.input()))
                        .andExpect(jsonPath("$.examples.examples[" + i + "].output")
                                .value(example.output()))
                        .andExpect(jsonPath("$.examples.examples[" + i + "].explanation")
                                .value(example.explanation()));
            }
        }
        if (problem.getConstraints() != null) {
            result.andExpect(jsonPath("$.constraints.executionTimeLimitMs")
                            .value(problem.getConstraints().executionTimeLimitMs()))
                    .andExpect(jsonPath("$.constraints.memoryUsageLimitBytes")
                            .value(problem.getConstraints().memoryUsageLimitBytes()));
        }
        if (timestamps) {
            result.andExpect(jsonPath("$.createdAt", notNullValue()))
                    .andExpect(jsonPath("$.updatedAt", notNullValue()))
                    .andExpect(
                            problem.getPublishedAt() == null
                                    ? jsonPath("$.publishedAt").value(nullValue())
                                    : jsonPath("$.publishedAt", notNullValue()));
        }
    }
}
