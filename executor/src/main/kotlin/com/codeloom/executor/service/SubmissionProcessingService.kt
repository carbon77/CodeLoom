package com.codeloom.executor.service

import com.codeloom.executor.engine.*
import com.codeloom.executor.event.SubmissionKafkaEvent
import com.codeloom.executor.languages.LanguageSpec
import com.codeloom.executor.repository.TestCaseRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.*

@Service
class SubmissionProcessingService(
    private val testCaseRepository: TestCaseRepository,
    private val dockerJudgeEngine: DockerJudgeEngine,
) {
    private val logger = LoggerFactory.getLogger(SubmissionProcessingService::class.java)

    fun process(event: SubmissionKafkaEvent) {
        val testCases = testCaseRepository.findByProblemId(event.problemId)
        if (testCases.isEmpty()) {
            logger.warn("Problem doesn't have any test cases: problemId={}", event.problemId)
            changeSubmissionState(event.submissionId, SubmissionState.ACCEPTED)
            return
        }

        val context = SubmissionContext(
            submissionId = event.submissionId,
            code = event.code,
            language = LanguageSpec.fromLanguage(event.language),
            executionTimeLimitMs = event.executionTimeLimitMs,
            memoryUsageLimitBytes = event.memoryUsageLimitBytes,
        )

        try {
            changeSubmissionState(event.submissionId, SubmissionState.COMPILING)
            val compilationResult = dockerJudgeEngine.compile(context)
            if (!compilationResult.isSuccessful) {
                changeSubmissionState(event.submissionId, SubmissionState.COMPILE_ERROR)
                return
            }

            changeSubmissionState(event.submissionId, SubmissionState.RUNNING)
            for (testCase in testCases) {
                val runResult = dockerJudgeEngine.runTestCase(context, testCase)

                if (runResult.exitCode != 0L) {
                    changeSubmissionState(
                        submissionId = event.submissionId,
                        state = when (runResult.exitCode) {
                            TIMEOUT_EXIT_CODE -> SubmissionState.TIME_LIMIT_EXCEEDED
                            MEMORY_LIMIT_EXCEEDED_EXIT_CODE -> SubmissionState.MEMORY_LIMIT_EXCEEDED
                            else -> SubmissionState.RUNTIME_ERROR
                        }
                    )
                    return
                }

                if (runResult.stdout != testCase.expectedOutput) {
                    changeSubmissionState(event.submissionId, SubmissionState.WRONG_ANSWER)
                    return
                }
            }
            changeSubmissionState(event.submissionId, SubmissionState.ACCEPTED)
        } finally {
            dockerJudgeEngine.cleanup(event.submissionId)
        }
    }

    fun changeSubmissionState(submissionId: UUID, state: SubmissionState) {
        logger.info("Submission(id={}) state changed to {}", submissionId, state)
    }
}