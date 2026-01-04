package com.codeloom.backend.it

import com.codeloom.backend.BaseTest
import com.codeloom.backend.dao.ProblemRepository
import com.codeloom.backend.model.*
import org.hamcrest.Matchers
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNotNull
import org.junit.jupiter.api.assertNull
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.web.servlet.*
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Sql(
    statements = [
        "TRUNCATE TABLE problems RESTART IDENTITY CASCADE"
    ],
    executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
)
class ProblemIT : BaseTest() {

    @Autowired
    private lateinit var problemRepository: ProblemRepository

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Nested
    inner class FindAllItems {

        @Test
        fun `test with no problems should return empty array`() {
            mockMvc
                .get("/v1/problems/items")
                .andExpect {
                    status { isOk() }
                    content { contentType(MediaType.APPLICATION_JSON) }
                    jsonPath("$.length()", Matchers.equalTo(0))
                }
        }

        @Test
        fun `test with problem should return array`() {
            problemRepository.save(
                Problem(
                    title = "Two Sum",
                    slug = "two_sum",
                )
            )
            problemRepository.save(
                Problem(
                    title = "Sort",
                    slug = "sort",
                    difficulty = ProblemDifficulty.MEDIUM,
                    publishedAt = Instant.now(),
                )
            )

            mockMvc
                .get("/v1/problems/items") {
                    param("publishedOnly", "false")
                }
                .andExpect {
                    status { isOk() }
                    content { contentType(MediaType.APPLICATION_JSON) }
                    jsonPath("$.length()", Matchers.equalTo(2))
                    jsonPath("$[0].title", Matchers.equalTo("Two Sum"))
                    jsonPath("$[0].slug", Matchers.equalTo("two_sum"))
                    jsonPath("$[0].difficulty", Matchers.equalTo("EASY"))
                    jsonPath("$[0].publishedAt", Matchers.nullValue())

                    jsonPath("$[1].title", Matchers.equalTo("Sort"))
                    jsonPath("$[1].slug", Matchers.equalTo("sort"))
                    jsonPath("$[1].difficulty", Matchers.equalTo("MEDIUM"))
                    jsonPath("$[1].publishedAt", Matchers.notNullValue())

                }
        }
    }

    @Nested
    inner class FindBySlug {
        @Test
        fun `test should return problem`() {
            val savedProblem = initProblem()
            mockMvc.get("/v1/problems/slug/two_sum")
                .andExpect {
                    status { isOk() }
                    content { contentTypeCompatibleWith(MediaType.APPLICATION_JSON) }
                    jsonPath("$.id", Matchers.equalTo(1))
                    checkProblem(savedProblem)
                }
        }

        @Test
        fun `test with non-existing slug should return 404`() {
            mockMvc.get("/v1/problems/slug/1j2hn")
                .andExpect {
                    status { isNotFound() }
                }
        }
    }

    @Nested
    inner class FindById {
        @Test
        fun `test should return problem`() {
            val savedProblem = initProblem()
            mockMvc.get("/v1/problems/1")
                .andExpect {
                    status { isOk() }
                    content { contentTypeCompatibleWith(MediaType.APPLICATION_JSON) }
                    jsonPath("$.id", Matchers.equalTo(1))
                    checkProblem(savedProblem)
                }
        }

        @Test
        fun `test with non-existing id should return 404`() {
            mockMvc.get("/v1/problems/3")
                .andExpect {
                    status { isNotFound() }
                }
        }
    }

    @Nested
    inner class Delete {
        @Test
        fun `test should delete`() {
            initProblem()
            mockMvc.delete("/v1/problems/1")
                .andExpect {
                    status { isOk() }
                }

            assertEquals(0, problemRepository.count())
        }
    }

    @Nested
    inner class Create {
        @Test
        fun `test should create`() {
            mockMvc.post("/v1/problems") {
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
            mockMvc.post("/v1/problems") {
                contentType = MediaType.APPLICATION_JSON
                content = """{ "title": "Merge two Arrays" }"""
            }
                .andExpect {
                    status { isOk() }
                }

            mockMvc.post("/v1/problems") {
                contentType = MediaType.APPLICATION_JSON
                content = """{ "title": "Merge two Arrays" }"""
            }
                .andExpect {
                    status { isBadRequest() }
                }

            assertEquals(1, problemRepository.count())
        }
    }

    @Nested
    inner class Publication {
        @Test
        fun `test publish should publish`() {
            initProblem()
            val problemBefore = problemRepository.findById(1).get()
            assertNull(problemBefore.publishedAt)
            assertEquals(problemBefore.createdAt, problemBefore.updatedAt)
            mockMvc.patch("/v1/problems/1/publish")
                .andExpect {
                    status { isOk() }
                }
            val problemAfter = problemRepository.findById(1).get()
            assertNotNull(problemAfter.publishedAt)
            assertNotEquals(problemAfter.createdAt, problemAfter.updatedAt)
        }

        @Test
        fun `test unpublish should unpublish`() {
            initProblem(Instant.now())
            val problemBefore = problemRepository.findById(1).get()
            assertNotNull(problemBefore.publishedAt)
            assertEquals(problemBefore.createdAt, problemBefore.updatedAt)
            mockMvc.patch("/v1/problems/1/unpublish")
                .andExpect {
                    status { isOk() }
                }
            val problemAfter = problemRepository.findById(1).get()
            assertNull(problemAfter.publishedAt)
            assertNotEquals(problemAfter.createdAt, problemAfter.updatedAt)
        }
    }

    private fun initProblem(publishedAt: Instant? = null): Problem {
        return problemRepository.save(
            Problem(
                title = "Two Sum",
                slug = "two_sum",
                difficulty = ProblemDifficulty.MEDIUM,
                description = "Find two numbers",
                publishedAt = publishedAt,
                hints = arrayOf("hint #1", "hint #2"),
                examples = ProblemExamples(
                    examples = listOf(ProblemExample("input", "output", "explain"))
                ),
                constraints = ProblemConstraints(
                    timeLimitMs = 2000,
                    memoryLimitMb = 4,
                )
            )
        )
    }

    private fun MockMvcResultMatchersDsl.checkProblem(problem: Problem) {
        jsonPath("$.title", Matchers.equalTo(problem.title))
        jsonPath("$.slug", Matchers.equalTo(problem.slug))
        jsonPath("$.difficulty", Matchers.equalTo(problem.difficulty.name))
        jsonPath("$.description", Matchers.equalTo(problem.description))
        jsonPath("$.hints.length()", Matchers.equalTo(problem.hints.size))
        problem.hints.forEachIndexed { i, hint ->
            jsonPath("$.hints[$i]", Matchers.equalTo(hint))
        }

        problem.examples?.let { (examples) ->
            jsonPath("$.examples.examples.length()", Matchers.equalTo(examples.size))
            examples.forEachIndexed { i, example ->
                jsonPath("$.examples.examples[$i].input", Matchers.equalTo(example.input))
                jsonPath("$.examples.examples[$i].output", Matchers.equalTo(example.output))
                jsonPath("$.examples.examples[$i].explanation", Matchers.equalTo(example.explanation))
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
}