package com.codeloom.executor.service

import com.codeloom.common.SubmissionEvent
import com.codeloom.common.SubmissionStatus
import com.codeloom.common.event.SubmissionStatusPayload
import com.codeloom.common.event.TestCaseResult
import com.codeloom.executor.engine.DockerJudgeEngine
import com.codeloom.executor.engine.MEMORY_LIMIT_EXCEEDED_EXIT_CODE
import com.codeloom.executor.engine.SubmissionContext
import com.codeloom.executor.engine.TIMEOUT_EXIT_CODE
import com.codeloom.common.language.LanguageSpec
import com.codeloom.executor.repository.TestCaseRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class SubmissionProcessingService(
    private val testCaseRepository: TestCaseRepository,
    private val dockerJudgeEngine: DockerJudgeEngine,
    private val eventService: EventService,
) {
    private val logger = LoggerFactory.getLogger(SubmissionProcessingService::class.java)

    fun process(event: SubmissionEvent) {
        val testCases = testCaseRepository.findByProblemId(event.problemId)
        val context =
            SubmissionContext(
                submissionId = event.submissionId,
                userId = event.userId,
                problemId = event.problemId,
                code = event.code,
                language = LanguageSpec.fromLanguage(event.language),
                executionTimeLimitMs = event.executionTimeLimitMs,
                memoryUsageLimitBytes = event.memoryUsageLimitBytes,
            )

        if (testCases.isEmpty()) {
            logger.warn("Problem doesn't have any test cases: problemId={}", event.problemId)
            changeSubmissionStatus(context, SubmissionStatus.ACCEPTED)
            return
        }

        val testCaseResults = mutableListOf<TestCaseResult>()
        try {
            changeSubmissionStatus(context, SubmissionStatus.COMPILING)
            val compilationResult = dockerJudgeEngine.compile(context)
            if (!compilationResult.isSuccessful) {
                changeSubmissionStatus(context, SubmissionStatus.COMPILE_ERROR)
                return
            }

            changeSubmissionStatus(context, SubmissionStatus.RUNNING)
            for (testCase in testCases) {
                val runResult = dockerJudgeEngine.runTestCase(context, testCase)

                if (testCase.isPublic) {
                    testCaseResults +=
                        TestCaseResult(
                            id = testCase.id!!,
                            problemId = testCase.problemId,
                            input = testCase.input,
                            expectedOutput = testCase.expectedOutput,
                            stdout = runResult.stdout,
                            stderr = runResult.stderr,
                            executionTimeMs = runResult.executionTimeMs,
                            memoryUsageBytes = runResult.memoryUsageBytes,
                        )
                }

                if (runResult.exitCode != 0L) {
                    val newStatus =
                        when (runResult.exitCode) {
                            TIMEOUT_EXIT_CODE -> SubmissionStatus.TIME_LIMIT_EXCEEDED
                            MEMORY_LIMIT_EXCEEDED_EXIT_CODE -> SubmissionStatus.MEMORY_LIMIT_EXCEEDED
                            else -> SubmissionStatus.RUNTIME_ERROR
                        }
                    changeSubmissionStatus(
                        context,
                        newStatus,
                        SubmissionStatusPayload(testCaseResults = testCaseResults),
                    )
                    return
                }

                if (runResult.stdout != testCase.expectedOutput) {
                    changeSubmissionStatus(
                        context,
                        SubmissionStatus.WRONG_ANSWER,
                        SubmissionStatusPayload(testCaseResults = testCaseResults),
                    )
                    return
                }
            }
            changeSubmissionStatus(
                context,
                SubmissionStatus.ACCEPTED,
                SubmissionStatusPayload(testCaseResults = testCaseResults),
            )
        } catch (e: Exception) {
            logger.error("Error while processing submission={}", context, e)
            changeSubmissionStatus(
                context,
                SubmissionStatus.SYSTEM_ERROR,
                SubmissionStatusPayload(testCaseResults = testCaseResults),
            )
        } finally {
            dockerJudgeEngine.cleanup(context.submissionId)
        }
    }

    fun changeSubmissionStatus(
        context: SubmissionContext,
        status: SubmissionStatus,
        payload: SubmissionStatusPayload? = null,
    ) {
        logger.info("Submission(id={}) status changed to {}", context.submissionId, status)
        eventService.submissionStatusChanged(context, status, payload)
    }
}
