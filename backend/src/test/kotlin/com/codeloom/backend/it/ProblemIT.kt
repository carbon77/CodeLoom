package com.codeloom.backend.it

import com.codeloom.backend.dao.ProblemRepository
import com.codeloom.backend.dao.ProblemTopicRepository
import com.codeloom.backend.dao.TopicRepository
import com.codeloom.backend.model.*
import org.hamcrest.Matchers
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNotNull
import org.junit.jupiter.api.assertNull
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.http.MediaType
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.web.servlet.*
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.postgresql.PostgreSQLContainer
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@AutoConfigureMockMvc(addFilters = false)
@Sql(
    statements =
        [
            "TRUNCATE TABLE problem_topics CASCADE",
            "TRUNCATE TABLE topics CASCADE",
            "TRUNCATE TABLE problems RESTART IDENTITY CASCADE",
        ],
    executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
)
class ProblemIT {
    companion object {
        @JvmStatic
        @Container
        @ServiceConnection
        val postgresContainer = PostgreSQLContainer("postgres:18.1-alpine3.23").apply {
            withDatabaseName("testdb")
            withUsername("testuser")
            withPassword("test")
        }
    }

    @Autowired
    private lateinit var problemTopicRepository: ProblemTopicRepository

    @Autowired
    private lateinit var problemRepository: ProblemRepository

    @Autowired
    private lateinit var topicRepository: TopicRepository

    @Autowired
    private lateinit var mockMvc: MockMvc

    private lateinit var ts: MutableList<Topic>
    private lateinit var ps: MutableList<Problem>

    @Nested
    inner class FindAllItems {

        @Test
        fun `test with no problems should return empty array`() {
            mockMvc.get("/v1/problems/items").andExpect {
                status { isOk() }
                content { contentType(MediaType.APPLICATION_JSON) }
                jsonPath("$.length()", Matchers.equalTo(0))
            }
        }

        @Test
        fun `test with problem should return array`() {
            initProblems()
            mockMvc.get("/v1/problems/items") { param("publishedOnly", "false") }.andExpect {
                status { isOk() }
                content { contentType(MediaType.APPLICATION_JSON) }
                jsonPath("$.length()", Matchers.equalTo(3))
                jsonPath("$[0].title", Matchers.equalTo("Two Sum"))
                jsonPath("$[0].slug", Matchers.equalTo("two_sum"))
                jsonPath("$[0].difficulty", Matchers.equalTo("EASY"))
                jsonPath("$[0].publishedAt", Matchers.nullValue())

                jsonPath("$[1].title", Matchers.equalTo("Sort"))
                jsonPath("$[1].slug", Matchers.equalTo("sort"))
                jsonPath("$[1].difficulty", Matchers.equalTo("MEDIUM"))
                jsonPath("$[1].publishedAt", Matchers.notNullValue())

                jsonPath("$[2].title", Matchers.equalTo("B-Tree Sort"))
                jsonPath("$[2].slug", Matchers.equalTo("b-tree_sort"))
                jsonPath("$[2].difficulty", Matchers.equalTo("HARD"))
                jsonPath("$[2].publishedAt", Matchers.notNullValue())
            }
        }
    }

    @Nested
    inner class FindBySlug {
        @Test
        fun `test should return problem`() {
            initProblems()
            mockMvc.get("/v1/problems/slug/two_sum").andExpect {
                status { isOk() }
                content { contentTypeCompatibleWith(MediaType.APPLICATION_JSON) }
                jsonPath("$.id", Matchers.equalTo(1))
                checkProblemDto(ps[0])
            }
        }

        @Test
        fun `test with non-existing slug should return 404`() {
            mockMvc.get("/v1/problems/slug/1j2hn").andExpect { status { isNotFound() } }
        }
    }

    @Nested
    inner class FindById {
        @Test
        fun `test should return problem`() {
            initProblems()
            mockMvc.get("/v1/problems/1").andExpect {
                status { isOk() }
                content { contentTypeCompatibleWith(MediaType.APPLICATION_JSON) }
                jsonPath("$.id", Matchers.equalTo(1))
                checkProblem(ps[0])
            }
        }

        @Test
        fun `test with non-existing id should return 404`() {
            mockMvc.get("/v1/problems/3123").andExpect { status { isNotFound() } }
        }
    }

    @Nested
    inner class Delete {
        @Test
        fun `test should delete`() {
            initProblems()
            mockMvc.delete("/v1/problems/1").andExpect { status { isOk() } }
            assertEquals(2, problemRepository.count())
        }
    }

    @Nested
    inner class Create {
        @Test
        fun `test should create`() {
            mockMvc
                .post("/v1/problems") {
                    contentType = MediaType.APPLICATION_JSON
                    content = """{ "title": "Merge two Arrays" }"""
                }
                .andExpect {
                    status { isOk() }
                    content { contentTypeCompatibleWith(MediaType.APPLICATION_JSON) }
                    jsonPath("$.id", Matchers.equalTo(1))
                    checkProblem(
                        Problem(
                            id = 1,
                            title = "Merge two Arrays",
                            slug = "merge_two_arrays",
                        )
                    )
                }
            assertEquals(1, problemRepository.count())
        }

        @Test
        fun `test with duplicated slug should return bad request`() {
            mockMvc
                .post("/v1/problems") {
                    contentType = MediaType.APPLICATION_JSON
                    content = """{ "title": "Merge two Arrays" }"""
                }
                .andExpect { status { isOk() } }

            mockMvc
                .post("/v1/problems") {
                    contentType = MediaType.APPLICATION_JSON
                    content = """{ "title": "Merge two Arrays" }"""
                }
                .andExpect { status { isBadRequest() } }

            assertEquals(1, problemRepository.count())
        }
    }

    @Nested
    inner class Update {
        @Test
        fun `test should update problem`() {
            initProblems()
            val problemBefore = problemRepository.findById(1).get()

            mockMvc
                .put("/v1/problems/1") {
                    contentType = MediaType.APPLICATION_JSON
                    content =
                        """
                    {
                        "title": "Three Sum",
                        "slug": "three_sum",
                        "difficulty": "HARD",
                        "description": "Find three numbers that sum to zero",
                        "hints": ["hint #1", "hint #2", "hint #3"],
                        "examples": {
                            "examples": [
                                {
                                    "input": "nums = [-1,0,1,2,-1,-4]",
                                    "output": "[[-1,-1,2],[-1,0,1]]",
                                    "explanation": "These triplets sum to zero"
                                }
                            ]
                        },
                        "constraints": {
                            "timeLimitMs": 3000,
                            "memoryLimitMb": 8
                        }
                    }
                """.trimIndent()
                }
                .andExpect {
                    status { isOk() }
                    content { contentTypeCompatibleWith(MediaType.APPLICATION_JSON) }
                    jsonPath("$.id", Matchers.equalTo(1))
                    jsonPath("$.title", Matchers.equalTo("Three Sum"))
                    jsonPath("$.slug", Matchers.equalTo("three_sum"))
                    jsonPath("$.difficulty", Matchers.equalTo("HARD"))
                    jsonPath(
                        "$.description",
                        Matchers.equalTo("Find three numbers that sum to zero")
                    )
                    jsonPath("$.hints.length()", Matchers.equalTo(3))
                    jsonPath("$.hints[0]", Matchers.equalTo("hint #1"))
                    jsonPath("$.hints[1]", Matchers.equalTo("hint #2"))
                    jsonPath("$.hints[2]", Matchers.equalTo("hint #3"))
                    jsonPath("$.examples.examples.length()", Matchers.equalTo(1))
                    jsonPath(
                        "$.examples.examples[0].input",
                        Matchers.equalTo("nums = [-1,0,1,2,-1,-4]")
                    )
                    jsonPath(
                        "$.examples.examples[0].output",
                        Matchers.equalTo("[[-1,-1,2],[-1,0,1]]")
                    )
                    jsonPath(
                        "$.examples.examples[0].explanation",
                        Matchers.equalTo("These triplets sum to zero")
                    )
                    jsonPath("$.constraints.timeLimitMs", Matchers.equalTo(3000))
                    jsonPath("$.constraints.memoryLimitMb", Matchers.equalTo(8))
                    jsonPath("$.createdAt", Matchers.notNullValue())
                    jsonPath("$.updatedAt", Matchers.notNullValue())
                }

            val problemAfter = problemRepository.findById(1).get()
            assertEquals("Three Sum", problemAfter.title)
            assertEquals("three_sum", problemAfter.slug)
            assertEquals(ProblemDifficulty.HARD, problemAfter.difficulty)
            assertEquals("Find three numbers that sum to zero", problemAfter.description)
            assertEquals(3, problemAfter.hints.size)
            assertNotEquals(problemBefore.updatedAt, problemAfter.updatedAt)
        }

        @Test
        fun `test with non-existing id should return 404`() {
            mockMvc
                .put("/v1/problems/999") {
                    contentType = MediaType.APPLICATION_JSON
                    content =
                        """
                    {
                        "title": "Updated Title",
                        "slug": "updated_slug"
                    }
                """.trimIndent()
                }
                .andExpect { status { isNotFound() } }
        }

        @Test
        fun `test should partially update problem fields`() {
            initProblems()
            mockMvc
                .put("/v1/problems/1") {
                    contentType = MediaType.APPLICATION_JSON
                    content =
                        """
                    {
                        "title": "Updated Two Sum",
                        "difficulty": "EASY"
                    }
                """.trimIndent()
                }
                .andExpect {
                    status { isOk() }
                    content { contentTypeCompatibleWith(MediaType.APPLICATION_JSON) }
                    jsonPath("$.id", Matchers.equalTo(1))
                    jsonPath("$.title", Matchers.equalTo("Updated Two Sum"))
                    jsonPath("$.difficulty", Matchers.equalTo("EASY"))
                    jsonPath("$.slug", Matchers.equalTo("two_sum"))
                }

            val problemAfter = problemRepository.findById(1).get()
            assertEquals("Updated Two Sum", problemAfter.title)
            assertEquals(ProblemDifficulty.EASY, problemAfter.difficulty)
        }

        @Test
        fun `test should update problem with topics and create new topics if they do not exist`() {
            initProblems()
            assertEquals(3, topicRepository.count())

            mockMvc
                .put("/v1/problems/1") {
                    contentType = MediaType.APPLICATION_JSON
                    content =
                        """
                    {
                        "title": "Three Sum",
                        "slug": "three_sum",
                        "topics": [
                            { "topic_id": "incorrect_uuid" },
                            { "topic_id": "${ts[0].id!!}" },
                            { "name": "Two Pointers" },
                            { "name": "Hash Table" }
                        ]
                    }
                """.trimIndent()
                }
                .andExpect {
                    status { isOk() }
                    content { contentTypeCompatibleWith(MediaType.APPLICATION_JSON) }
                    jsonPath("$.id", Matchers.equalTo(1))
                    jsonPath("$.title", Matchers.equalTo("Three Sum"))
                    jsonPath("$.slug", Matchers.equalTo("three_sum"))
                }

            // Verify topics were created
            assertEquals(5, topicRepository.count())
            val createdTopics = topicRepository.findAll().toList()
            val topicNames = createdTopics.map { it.name }.toSet()
            assertEquals(setOf("topic1", "topic2", "topic3", "Two Pointers", "Hash Table"), topicNames)

            // Verify problem is linked to the topics
            val problemTopics = topicRepository.findByProblemId(1).toList()
            assertEquals(3, problemTopics.size)
            val linkedTopicNames = problemTopics.map { it.name }.toSet()
            assertEquals(setOf("topic1", "Two Pointers", "Hash Table"), linkedTopicNames)
        }
    }

    @Nested
    inner class Publication {
        @Test
        fun `test publish should publish`() {
            initProblems()
            val problemBefore = problemRepository.findById(1).get()
            assertNull(problemBefore.publishedAt)
            assertEquals(problemBefore.createdAt, problemBefore.updatedAt)
            mockMvc.patch("/v1/problems/1/publish").andExpect { status { isOk() } }
            val problemAfter = problemRepository.findById(1).get()
            assertNotNull(problemAfter.publishedAt)
            assertNotEquals(problemAfter.createdAt, problemAfter.updatedAt)
        }

        @Test
        fun `test unpublish should unpublish`() {
            initProblems()
            val problemBefore = problemRepository.findById(2).get()
            assertNotNull(problemBefore.publishedAt)
            assertEquals(problemBefore.createdAt, problemBefore.updatedAt)
            mockMvc.patch("/v1/problems/2/unpublish").andExpect { status { isOk() } }
            val problemAfter = problemRepository.findById(2).get()
            assertNull(problemAfter.publishedAt)
            assertNotEquals(problemAfter.createdAt, problemAfter.updatedAt)
        }
    }


    fun initProblems() {
        ts = mutableListOf()
        ps = mutableListOf()

        val t1 = topicRepository.save(Topic(name = "topic1"))
        val t2 = topicRepository.save(Topic(name = "topic2"))
        val t3 = topicRepository.save(Topic(name = "topic3"))
        val p1 = problemRepository.save(
            Problem(
                title = "Two Sum",
                slug = "two_sum",
                description = "Find two numbers",
                hints = arrayOf("hint #1", "hint #2"),
                examples =
                    ProblemExamples(
                        examples =
                            listOf(ProblemExample("input", "output", "explain"))
                    ),
                constraints =
                    ProblemConstraints(
                        timeLimitMs = 2000,
                        memoryLimitMb = 4,
                    )
            )
        )
        val p2 = problemRepository.save(
            Problem(
                title = "Sort",
                slug = "sort",
                difficulty = ProblemDifficulty.MEDIUM,
                publishedAt = Instant.now(),
            )
        )
        val p3 = problemRepository.save(
            Problem(
                title = "B-Tree Sort",
                slug = "b-tree_sort",
                difficulty = ProblemDifficulty.HARD,
                publishedAt = Instant.now(),
            )
        )
        ts.addAll(listOf(t1, t2, t3))
        ps.addAll(listOf(p1, p2, p3))
        problemTopicRepository.saveAll(
            listOf(
                t3 to p1,
                t1 to p2,
                t2 to p2,
                t3 to p2,
                t2 to p3,
                t3 to p3,
            )
        )
    }

    private fun MockMvcResultMatchersDsl.checkProblem(problem: Problem) {
        jsonPath("$.title", Matchers.equalTo(problem.title))
        jsonPath("$.slug", Matchers.equalTo(problem.slug))
        jsonPath("$.difficulty", Matchers.equalTo(problem.difficulty.name))
        jsonPath("$.description", Matchers.equalTo(problem.description))
        jsonPath("$.hints.length()", Matchers.equalTo(problem.hints.size))
        problem.hints.forEachIndexed { i, hint -> jsonPath("$.hints[$i]", Matchers.equalTo(hint)) }

        problem.examples?.let { (examples) ->
            jsonPath("$.examples.examples.length()", Matchers.equalTo(examples.size))
            examples.forEachIndexed { i, example ->
                jsonPath("$.examples.examples[$i].input", Matchers.equalTo(example.input))
                jsonPath("$.examples.examples[$i].output", Matchers.equalTo(example.output))
                jsonPath(
                    "$.examples.examples[$i].explanation",
                    Matchers.equalTo(example.explanation)
                )
            }
        }

        problem.constraints?.let {
            jsonPath("$.constraints.timeLimitMs", Matchers.equalTo(it.timeLimitMs))
            jsonPath("$.constraints.memoryLimitMb", Matchers.equalTo(it.memoryLimitMb))
        }

        jsonPath("$.createdAt", Matchers.notNullValue())
        jsonPath("$.updatedAt", Matchers.notNullValue())

        if (problem.publishedAt != null) {
            jsonPath("$.publishedAt", Matchers.notNullValue())
        } else {
            jsonPath("$.publishedAt", Matchers.nullValue())
        }
    }

    private fun MockMvcResultMatchersDsl.checkProblemDto(problem: Problem) {
        jsonPath("$.title", Matchers.equalTo(problem.title))
        jsonPath("$.slug", Matchers.equalTo(problem.slug))
        jsonPath("$.difficulty", Matchers.equalTo(problem.difficulty.name))
        jsonPath("$.description", Matchers.equalTo(problem.description))
        jsonPath("$.hints.length()", Matchers.equalTo(problem.hints.size))
        problem.hints.forEachIndexed { i, hint -> jsonPath("$.hints[$i]", Matchers.equalTo(hint)) }

        problem.examples?.let { (examples) ->
            jsonPath("$.examples.examples.length()", Matchers.equalTo(examples.size))
            examples.forEachIndexed { i, example ->
                jsonPath("$.examples.examples[$i].input", Matchers.equalTo(example.input))
                jsonPath("$.examples.examples[$i].output", Matchers.equalTo(example.output))
                jsonPath(
                    "$.examples.examples[$i].explanation",
                    Matchers.equalTo(example.explanation)
                )
            }
        }

        problem.constraints?.let {
            jsonPath("$.constraints.timeLimitMs", Matchers.equalTo(it.timeLimitMs))
            jsonPath("$.constraints.memoryLimitMb", Matchers.equalTo(it.memoryLimitMb))
        }
    }
}
