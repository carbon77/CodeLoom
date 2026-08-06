package com.codeloom.backend.it

import com.codeloom.backend.dao.problem.ProblemRepository
import com.codeloom.backend.dao.SubmissionRepository
import com.codeloom.backend.dao.testcase.TestCaseRepository
import com.codeloom.backend.dao.testcase.TestCaseResultRepository
import com.codeloom.backend.model.Problem
import com.codeloom.backend.model.Submission
import com.codeloom.backend.model.TestCase
import com.codeloom.common.SubmissionStatus
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.test.context.TestPropertySource
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.kafka.ConfluentKafkaContainer
import org.testcontainers.postgresql.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName
import java.util.*
import kotlin.test.assertEquals

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@TestPropertySource(
    properties = [
        "spring.kafka.consumer.auto-offset-reset=earliest",
    ]
)
class SubmissionStatusConsumerIT {

    companion object {
        @JvmStatic
        @Container
        @ServiceConnection
        val postgresContainer = PostgreSQLContainer("postgres:18.1-alpine3.23").apply {
            withDatabaseName("testdb")
            withUsername("testuser")
            withPassword("test")
        }

        @JvmStatic
        @Container
        @ServiceConnection
        val kafkaContainer = ConfluentKafkaContainer(
            DockerImageName.parse("confluentinc/cp-kafka:7.7.7")
        )
    }

    @Autowired
    private lateinit var problemRepository: ProblemRepository

    @Autowired
    private lateinit var testCaseRepository: TestCaseRepository

    @Autowired
    private lateinit var submissionRepository: SubmissionRepository

    @Autowired
    private lateinit var testCaseResultRepository: TestCaseResultRepository

    @Autowired
    private lateinit var kafkaTemplate: KafkaTemplate<String, String>

    @Value("\${codeloom.kafka.submission-status-topic}")
    private lateinit var topic: String

    private var problemId: Long = 0L
    private lateinit var submissionId: UUID
    private lateinit var publicTestCase: TestCase

    @BeforeEach
    fun setUp() {
        testCaseResultRepository.deleteAll()
        submissionRepository.deleteAll()
        testCaseRepository.deleteAll()
        problemRepository.deleteAll()

        val problem = problemRepository.save(Problem(title = "Two Sum", slug = "two_sum_${UUID.randomUUID()}"))
        problemId = problem.id!!
        publicTestCase =
            testCaseRepository.save(
                TestCase(
                    problemId = problemId,
                    input = "1",
                    expectedOutput = "1",
                    isPublic = true,
                )
            )
        testCaseRepository.save(
            TestCase(
                problemId = problemId,
                input = "2",
                expectedOutput = "2",
                isPublic = false,
            )
        )
        submissionId =
            submissionRepository.save(
                Submission(
                    userId = UUID.randomUUID(),
                    problemId = problemId,
                    code = "print(input())",
                    language = "python",
                    status = SubmissionStatus.PENDING,
                )
            ).id!!
    }

    @Test
    fun `should update submission status and persist test case results`() {
        kafkaTemplate.send(topic, submissionId.toString(), acceptedEvent()).get()

        awaitStatus(SubmissionStatus.ACCEPTED)
        awaitResultCount(1)

        val result = testCaseResultRepository.findAll().single()
        assertEquals(submissionId, result.submissionId)
        assertEquals("1", result.input)
        assertEquals("1", result.expectedOutput)
        assertEquals("1", result.stdout)
        assertEquals("", result.stderr)
        assertEquals(12L, result.executionTimeMs)
        assertEquals(1024L, result.bytesUsed)
    }

    @Test
    fun `should not persist test case results when payload is absent`() {
        kafkaTemplate.send(topic, submissionId.toString(), statusEvent(SubmissionStatus.COMPILING)).get()

        awaitStatus(SubmissionStatus.COMPILING)
        Thread.sleep(1000)
        assertEquals(0L, testCaseResultRepository.count())
    }

    @Test
    fun `should replace persisted results when a later status event arrives`() {
        kafkaTemplate.send(topic, submissionId.toString(), wrongAnswerEvent()).get()

        awaitStatus(SubmissionStatus.WRONG_ANSWER)
        awaitResultCount(1)

        kafkaTemplate.send(topic, submissionId.toString(), acceptedEvent()).get()

        awaitStatus(SubmissionStatus.ACCEPTED)
        awaitResultCount(1)

        val result = testCaseResultRepository.findAll().single()
        assertEquals("1", result.stdout)
    }

    @Test
    fun `should ignore events for unknown submissions`() {
        val unknownId = UUID.randomUUID()
        val event =
            """
            {
                "submissionId": "$unknownId",
                "userId": "${UUID.randomUUID()}",
                "problemId": $problemId,
                "newStatus": "ACCEPTED",
                "payload": {
                    "testCaseResults": [
                        {
                            "id": "${publicTestCase.id!!}",
                            "problemId": $problemId,
                            "input": "1",
                            "expectedOutput": "1",
                            "stdout": "1",
                            "stderr": "",
                            "executionTimeMs": 12,
                            "memoryUsageBytes": 1024
                        }
                    ]
                }
            }
        """.trimIndent()

        kafkaTemplate.send(topic, unknownId.toString(), event).get()

        Thread.sleep(2000)
        assertEquals(0L, testCaseResultRepository.count())
        assertEquals(SubmissionStatus.PENDING, submissionRepository.findById(submissionId).get().status)
    }

    private fun acceptedEvent(): String =
        event(SubmissionStatus.ACCEPTED, "1", "")

    private fun wrongAnswerEvent(): String =
        event(SubmissionStatus.WRONG_ANSWER, "0", "wrong")

    private fun event(status: SubmissionStatus, stdout: String, stderr: String): String {
        val results =
            """
            [
                {
                    "id": "${publicTestCase.id!!}",
                    "problemId": $problemId,
                    "input": "1",
                    "expectedOutput": "1",
                    "stdout": "$stdout",
                    "stderr": "$stderr",
                    "executionTimeMs": 12,
                    "memoryUsageBytes": 1024
                }
            ]
        """.trimIndent()
        return statusEventWithResults(status, results)
    }

    private fun statusEvent(status: SubmissionStatus): String =
        """
        {
            "submissionId": "$submissionId",
            "userId": "${UUID.randomUUID()}",
            "problemId": $problemId,
            "newStatus": "${status.name}",
            "payload": null
        }
    """.trimIndent()

    private fun statusEventWithResults(status: SubmissionStatus, resultsJson: String): String =
        """
        {
            "submissionId": "$submissionId",
            "userId": "${UUID.randomUUID()}",
            "problemId": $problemId,
            "newStatus": "${status.name}",
            "payload": {
                "testCaseResults": $resultsJson
            }
        }
    """.trimIndent()

    private fun awaitStatus(expected: SubmissionStatus, timeoutMs: Long = 20_000) {
        val deadline = System.currentTimeMillis() + timeoutMs
        while (System.currentTimeMillis() < deadline) {
            if (submissionRepository.findById(submissionId).get().status == expected) return
            Thread.sleep(100)
        }
        throw AssertionError("Submission $submissionId did not reach status $expected within ${timeoutMs}ms")
    }

    private fun awaitResultCount(expected: Int, timeoutMs: Long = 20_000) {
        val deadline = System.currentTimeMillis() + timeoutMs
        while (System.currentTimeMillis() < deadline) {
            val count = testCaseResultRepository.findAll().count { it.submissionId == submissionId }
            if (count == expected) return
            Thread.sleep(100)
        }
        throw AssertionError("Expected $expected test case result(s) for $submissionId within ${timeoutMs}ms")
    }
}
