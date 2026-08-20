package com.codeloom.executor.service

import com.codeloom.common.SubmissionEvent
import com.codeloom.common.SubmissionStatus
import com.codeloom.common.event.SubmissionStatusPayload
import com.codeloom.executor.engine.CompilationResult
import com.codeloom.executor.engine.DockerJudgeEngine
import com.codeloom.executor.engine.RunResult
import com.codeloom.executor.model.TestCase
import com.codeloom.executor.repository.TestCaseRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.kotlin.*
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class SubmissionProcessingPayloadTest {
    private val testCaseRepository: TestCaseRepository = mock()
    private val dockerJudgeEngine: DockerJudgeEngine = mock()
    private val eventService: EventService = mock()

    private val service = SubmissionProcessingService(testCaseRepository, dockerJudgeEngine, eventService)

    private val submissionId = UUID.randomUUID()
    private val problemId = 1L

    private val publicTestCase =
        TestCase(id = UUID.randomUUID(), problemId = problemId, input = "1", expectedOutput = "1", isPublic = true)
    private val hiddenTestCase =
        TestCase(id = UUID.randomUUID(), problemId = problemId, input = "2", expectedOutput = "2", isPublic = false)

    private val event =
        SubmissionEvent(
            submissionId = submissionId,
            userId = UUID.randomUUID(),
            problemId = problemId,
            code = "print(input())",
            language = "python",
        )

    @BeforeEach
    fun setUp() {
        whenever(dockerJudgeEngine.compile(any())).thenReturn(CompilationResult(isSuccessful = true, stderr = ""))
    }

    @Test
    fun `accepted submission includes only public test case results`() {
        whenever(testCaseRepository.findByProblemId(problemId)).thenReturn(listOf(publicTestCase, hiddenTestCase))
        whenever(dockerJudgeEngine.runTestCase(any(), any())).thenAnswer { invocation ->
            val testCase = invocation.getArgument<TestCase>(1)
            RunResult(
                exitCode = 0L,
                stdout = testCase.expectedOutput,
                stderr = "",
                executionTimeMs = 10,
                memoryUsageBytes = 100,
            )
        }

        service.process(event)

        val payload = capturedPayload(SubmissionStatus.ACCEPTED)
        assertNotNull(payload)
        assertEquals(1, payload.testCaseResults!!.size)
        val result = payload.testCaseResults!!.single()
        assertEquals(publicTestCase.id, result.id)
        assertEquals(publicTestCase.input, result.input)
        assertEquals(publicTestCase.expectedOutput, result.expectedOutput)
        assertEquals("1", result.stdout)
        assertEquals("", result.stderr)
        assertEquals(10L, result.executionTimeMs)
        assertEquals(100L, result.memoryUsageBytes)
    }

    @Test
    fun `wrong answer includes results up to the failing test case`() {
        whenever(testCaseRepository.findByProblemId(problemId)).thenReturn(listOf(publicTestCase))
        whenever(dockerJudgeEngine.runTestCase(any(), any())).thenReturn(
            RunResult(exitCode = 0L, stdout = "0", stderr = "", executionTimeMs = 10, memoryUsageBytes = 100),
        )

        service.process(event)

        val payload = capturedPayload(SubmissionStatus.WRONG_ANSWER)
        assertNotNull(payload)
        assertEquals(1, payload.testCaseResults!!.size)
        assertEquals("0", payload.testCaseResults!!.single().stdout)
    }

    @Test
    fun `compile error carries no payload`() {
        whenever(testCaseRepository.findByProblemId(problemId)).thenReturn(listOf(publicTestCase))
        whenever(dockerJudgeEngine.compile(any())).thenReturn(
            CompilationResult(isSuccessful = false, stderr = "compile error"),
        )

        service.process(event)

        assertNull(capturedPayload(SubmissionStatus.COMPILE_ERROR))
    }

    private fun capturedPayload(status: SubmissionStatus): SubmissionStatusPayload? {
        val captor =
            ArgumentCaptor.forClass(SubmissionStatusPayload::class.java) as ArgumentCaptor<SubmissionStatusPayload>
        verify(eventService).submissionStatusChanged(any(), eq(status), captor.capture())
        return captor.value
    }
}
