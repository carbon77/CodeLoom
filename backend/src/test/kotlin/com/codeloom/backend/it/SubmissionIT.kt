package com.codeloom.backend.it

import com.codeloom.backend.dao.SubmissionRepository
import com.codeloom.backend.dao.problem.ProblemRepository
import com.codeloom.backend.model.Problem
import com.codeloom.backend.model.Submission
import com.codeloom.backend.security.UserRole
import com.codeloom.common.SubmissionEvent
import com.codeloom.common.SubmissionStatus
import org.hamcrest.Matchers
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.postgresql.PostgreSQLContainer
import tools.jackson.databind.ObjectMapper
import java.time.Instant
import java.util.*
import kotlin.test.assertEquals

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@AutoConfigureMockMvc(addFilters = false)
@Sql(
    statements =
        [
            "TRUNCATE TABLE test_case_results CASCADE",
            "TRUNCATE TABLE submissions CASCADE",
            "TRUNCATE TABLE problems RESTART IDENTITY CASCADE",
        ],
    executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD,
)
class SubmissionIT {
    companion object {
        @JvmStatic
        @Container
        @ServiceConnection
        val postgresContainer =
            PostgreSQLContainer("postgres:18.1-alpine3.23").apply {
                withDatabaseName("testdb")
                withUsername("testuser")
                withPassword("test")
            }
    }

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var problemRepository: ProblemRepository

    @Autowired
    private lateinit var submissionRepository: SubmissionRepository

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockitoBean
    private lateinit var kafkaTemplate: KafkaTemplate<String, String>

    @Nested
    inner class FindSubmissions {
        @Test
        fun `should return only submissions owned by authenticated user for problem`() {
            val problem = initProblem(published = true)
            val problemId = requireNotNull(problem.id)
            val ownSubmission = initSubmission(problemId, TEST_USER_ID, code = "own code")
            initSubmission(problemId, UUID.randomUUID(), code = "other code")
            val otherProblem = initProblem(published = true, title = "Sort", slug = "sort")
            initSubmission(otherProblem.id!!, TEST_USER_ID, code = "other problem code")

            mockMvc.get("/v1/submissions") {
                initUser()
                param("problemId", problemId.toString())
            }.andExpect {
                status { isOk() }
                content { contentTypeCompatibleWith(MediaType.APPLICATION_JSON) }
                jsonPath("$.length()", Matchers.equalTo(1))
                jsonPath("$[0].id", Matchers.equalTo(ownSubmission.id.toString()))
                jsonPath("$[0].userId", Matchers.equalTo(TEST_USER_ID.toString()))
                jsonPath("$[0].problemId", Matchers.equalTo(problemId.toInt()))
                jsonPath("$[0].code", Matchers.equalTo("own code"))
            }
        }

        @Test
        fun `should return empty array when user has no submissions`() {
            val problem = initProblem(published = true)

            mockMvc.get("/v1/submissions") {
                initUser()
                param("problemId", problem.id.toString())
            }.andExpect {
                status { isOk() }
                jsonPath("$.length()", Matchers.equalTo(0))
            }
        }
    }

    @Nested
    inner class SendSubmission {
        @Test
        fun `user should create pending submission and publish event for published problem`() {
            val problem = initProblem(published = true)

            mockMvc.post("/v1/submissions") {
                initUser(roles = arrayOf(UserRole.USER))
                contentType = MediaType.APPLICATION_JSON
                content = requestBody(problem.id!!)
            }.andExpect { status { isOk() } }

            val submission = submissionRepository.findAll().single()
            assertEquals(TEST_USER_ID, submission.userId)
            assertEquals(problem.id, submission.problemId)
            assertEquals("println(42)", submission.code)
            assertEquals("java", submission.language)
            assertEquals(SubmissionStatus.PENDING, submission.status)

            val valueCaptor = argumentCaptor<String>()
            verify(kafkaTemplate).send(eq("test-submissions"), eq(submission.id.toString()), valueCaptor.capture())
            val event = objectMapper.readValue(valueCaptor.firstValue, SubmissionEvent::class.java)
            assertEquals(submission.id, event.submissionId)
            assertEquals(TEST_USER_ID, event.userId)
            assertEquals(problem.id, event.problemId)
            assertEquals("println(42)", event.code)
            assertEquals("java", event.language)
        }

        @Test
        fun `admin should submit to unpublished problem`() {
            val problem = initProblem(published = false)

            mockMvc.post("/v1/submissions") {
                initUser()
                contentType = MediaType.APPLICATION_JSON
                content = requestBody(problem.id!!)
            }.andExpect { status { isOk() } }

            assertEquals(1, submissionRepository.count())
            verify(kafkaTemplate).send(eq("test-submissions"), any(), any())
        }

        @Test
        fun `user should not submit to unpublished problem`() {
            val problem = initProblem(published = false)

            mockMvc.post("/v1/submissions") {
                initUser(roles = arrayOf(UserRole.USER))
                contentType = MediaType.APPLICATION_JSON
                content = requestBody(problem.id!!)
            }.andExpect {
                status { isNotFound() }
                jsonPath("$.status", Matchers.equalTo(404))
            }

            assertEquals(0, submissionRepository.count())
            verify(kafkaTemplate, never()).send(eq("test-submissions"), any(), any())
        }

        @Test
        fun `should return not found for missing problem`() {
            mockMvc.post("/v1/submissions") {
                initUser(roles = arrayOf(UserRole.USER))
                contentType = MediaType.APPLICATION_JSON
                content = requestBody(999)
            }.andExpect { status { isNotFound() } }

            assertEquals(0, submissionRepository.count())
            verify(kafkaTemplate, never()).send(eq("test-submissions"), any(), any())
        }

        @Test
        fun `should reject blank code and language`() {
            val problem = initProblem(published = true)

            mockMvc.post("/v1/submissions") {
                initUser(roles = arrayOf(UserRole.USER))
                contentType = MediaType.APPLICATION_JSON
                content =
                    """
                    {
                      "problemId": ${problem.id},
                      "code": "",
                      "language": ""
                    }
                    """.trimIndent()
            }.andExpect {
                status { isBadRequest() }
                jsonPath("$.status", Matchers.equalTo(400))
                jsonPath("$.payload.code", Matchers.notNullValue())
                jsonPath("$.payload.language", Matchers.notNullValue())
            }

            assertEquals(0, submissionRepository.count())
            verify(kafkaTemplate, never()).send(eq("test-submissions"), any(), any())
        }

        @Test
        fun `should reject unsupported submission language`() {
            val problem = initProblem(published = true)

            mockMvc.post("/v1/submissions") {
                initUser(roles = arrayOf(UserRole.USER))
                contentType = MediaType.APPLICATION_JSON
                content =
                    """
                    {
                      "problemId": ${problem.id},
                      "code": "puts 42",
                      "language": "unknown"
                    }
                    """.trimIndent()
            }.andExpect {
                status { isBadRequest() }
                jsonPath("$.status", Matchers.equalTo(400))
                jsonPath("$.payload.language", Matchers.equalTo("Invalid submission language"))
            }

            assertEquals(0, submissionRepository.count())
            verify(kafkaTemplate, never()).send(eq("test-submissions"), any(), any())
        }
    }

    private fun initProblem(
        published: Boolean,
        title: String = "Two Sum",
        slug: String = "two_sum",
    ): Problem =
        problemRepository.save(
            Problem(
                title = title,
                slug = slug,
                publishedAt = if (published) Instant.now() else null,
            ),
        )

    private fun initSubmission(
        problemId: Long,
        userId: UUID,
        code: String,
    ): Submission =
        submissionRepository.save(
            Submission(
                userId = userId,
                problemId = problemId,
                code = code,
                status = SubmissionStatus.PENDING,
                language = "java",
            ),
        )

    private fun requestBody(problemId: Long): String =
        """
        {
          "problemId": $problemId,
          "code": "println(42)",
          "language": "java"
        }
        """.trimIndent()
}
