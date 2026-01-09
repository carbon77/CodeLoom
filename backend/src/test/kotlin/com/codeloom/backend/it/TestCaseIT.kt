package com.codeloom.backend.it

import com.codeloom.backend.BaseTest
import com.codeloom.backend.dao.ProblemRepository
import com.codeloom.backend.dao.TestCaseRepository
import com.codeloom.backend.model.Problem
import com.codeloom.backend.model.TestCase
import org.hamcrest.Matchers
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.MockMvcResultMatchersDsl
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.patch
import org.springframework.test.web.servlet.post
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Sql(
    statements = [
        "TRUNCATE TABLE test_cases RESTART IDENTITY CASCADE",
        "TRUNCATE TABLE problems RESTART IDENTITY CASCADE"
    ],
    executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
)
class TestCaseIT : BaseTest() {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var testCaseRepository: TestCaseRepository

    @Autowired
    private lateinit var problemRepository: ProblemRepository

    private fun initProblem(): Problem =
        problemRepository.save(
            Problem(
                title = "Two Sum",
                slug = "two_sum"
            )
        )

    private fun initTestCase(
        problemId: Long,
        isPublic: Boolean = true,
        input: String = "1 2",
        output: String = "3"
    ): TestCase =
        testCaseRepository.save(
            TestCase(
                problemId = problemId,
                input = input,
                expectedOutput = output,
                isPublic = isPublic
            )
        )

    /* -------------------------------------------------------------
     * GET /v1/testCases/{id}
     * ------------------------------------------------------------- */
    @Nested
    inner class FindOne {

        @Test
        fun `should return test case`() {
            val problem = initProblem()
            val testCase = initTestCase(problem.id!!)

            mockMvc.get("/v1/testCases/${testCase.id}")
                .andExpect {
                    status { isOk() }
                    content { contentTypeCompatibleWith(MediaType.APPLICATION_JSON) }
                    jsonPath("$.id", Matchers.equalTo(testCase.id.toString()))
                    checkTestCase(testCase)
                }
        }

        @Test
        fun `should return 404 for non-existing id`() {
            mockMvc.get("/v1/testCases/${UUID.randomUUID()}")
                .andExpect {
                    status { isNotFound() }
                }
        }
    }

    /* -------------------------------------------------------------
     * GET /v1/testCases/by-ids
     * ------------------------------------------------------------- */
    @Nested
    inner class FindMany {

        @Test
        fun `should return empty list`() {
            mockMvc.get("/v1/testCases/by-ids") {
                param("ids", UUID.randomUUID().toString())
            }
                .andExpect {
                    status { isOk() }
                    jsonPath("$.length()", Matchers.equalTo(0))
                }
        }

        @Test
        fun `should return test cases by ids`() {
            val problem = initProblem()
            val tc1 = initTestCase(problem.id!!)
            val tc2 = initTestCase(problem.id!!, input = "2 2", output = "4")

            mockMvc.get("/v1/testCases/by-ids") {
                param("ids", tc1.id.toString(), tc2.id.toString())
            }
                .andExpect {
                    status { isOk() }
                    jsonPath("$.length()", Matchers.equalTo(2))
                }
        }
    }

    /* -------------------------------------------------------------
     * GET /v1/testCases/by-problem-id/{problemId}
     * ------------------------------------------------------------- */
    @Nested
    inner class FindByProblemId {

        @Test
        fun `should return only public test cases by default`() {
            val problem = initProblem()
            initTestCase(problem.id!!, isPublic = true)
            initTestCase(problem.id!!, isPublic = false)

            mockMvc.get("/v1/testCases/by-problem-id/${problem.id}")
                .andExpect {
                    status { isOk() }
                    jsonPath("$.length()", Matchers.equalTo(1))
                }
        }

        @Test
        fun `should return all test cases when publicOnly=false`() {
            val problem = initProblem()
            initTestCase(problem.id!!, isPublic = true)
            initTestCase(problem.id!!, isPublic = false)

            mockMvc.get("/v1/testCases/by-problem-id/${problem.id}") {
                param("publicOnly", "false")
            }
                .andExpect {
                    status { isOk() }
                    jsonPath("$.length()", Matchers.equalTo(2))
                }
        }
    }

    /* -------------------------------------------------------------
     * POST /v1/testCases
     * ------------------------------------------------------------- */
    @Nested
    inner class Create {

        @Test
        fun `should create test case`() {
            val problem = initProblem()

            mockMvc.post("/v1/testCases") {
                contentType = MediaType.APPLICATION_JSON
                content = """
                    {
                      "problemId": ${problem.id},
                      "input": "1 2",
                      "expectedOutput": "3",
                      "isPublic": true
                    }
                """.trimIndent()
            }
                .andExpect {
                    status { isOk() }
                    jsonPath("$.id", Matchers.notNullValue())
                    jsonPath("$.problemId", Matchers.equalTo(problem.id!!.toInt()))
                    jsonPath("$.input", Matchers.equalTo("1 2"))
                    jsonPath("$.expectedOutput", Matchers.equalTo("3"))
                    jsonPath("$.isPublic", Matchers.equalTo(true))
                }

            assertEquals(1, testCaseRepository.count())
        }
    }

    /* -------------------------------------------------------------
     * PATCH /v1/testCases/{id}
     * ------------------------------------------------------------- */
    @Nested
    inner class Patch {

        @Test
        fun `should patch test case`() {
            val problem = initProblem()
            val testCase = initTestCase(problem.id!!, isPublic = false)

            mockMvc.patch("/v1/testCases/${testCase.id}") {
                contentType = MediaType.APPLICATION_JSON
                content = """
                    {
                      "isPublic": true
                    }
                """.trimIndent()
            }
                .andExpect {
                    status { isOk() }
                    jsonPath("$.isPublic", Matchers.equalTo(true))
                }

            val updated = testCaseRepository.findById(testCase.id!!).get()
            assertTrue(updated.isPublic)
        }
    }

    /* -------------------------------------------------------------
     * DELETE /v1/testCases/{id}
     * ------------------------------------------------------------- */
    @Nested
    inner class Delete {

        @Test
        fun `should delete test case`() {
            val problem = initProblem()
            val testCase = initTestCase(problem.id!!)

            mockMvc.delete("/v1/testCases/${testCase.id}")
                .andExpect {
                    status { isOk() }
                }

            assertEquals(0, testCaseRepository.count())
        }
    }

    /* -------------------------------------------------------------
     * JSON assertions
     * ------------------------------------------------------------- */
    private fun MockMvcResultMatchersDsl.checkTestCase(testCase: TestCase) {
        jsonPath("$.problemId", Matchers.equalTo(testCase.problemId.toInt()))
        jsonPath("$.input", Matchers.equalTo(testCase.input))
        jsonPath("$.expectedOutput", Matchers.equalTo(testCase.expectedOutput))
        jsonPath("$.isPublic", Matchers.equalTo(testCase.isPublic))
    }
}
