package com.codeloom.executor.service

import com.codeloom.executor.dto.CodeExecutionRequest
import com.codeloom.executor.event.SubmissionEvent
import com.codeloom.executor.repository.TestCaseRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class SubmissionProcessingService(
    private val testCaseRepository: TestCaseRepository,
    private val codeExecutorService: CodeExecutorService,
) {
    private val logger = LoggerFactory.getLogger(SubmissionProcessingService::class.java)

    fun process(event: SubmissionEvent) {
        val testCases = testCaseRepository.findByProblemId(event.problemId)
        if (testCases.isEmpty()) {
            logger.warn("Problem doesn't have any test cases: problemId={}", event.problemId)
            return
        }

        logger.info("Processing submission: submissionId={}, problemId={}", event.submissionId, event.problemId)
        for (testCase in testCases) {
            val request = CodeExecutionRequest(
                code = event.code,
                language = event.language,
                input = testCase.input,
            )
            val result = codeExecutorService.run(request)
            if (result.exitCode != 0) {
                logger.error("Submission failed: exitCode={}, stderr={}", result.exitCode, result.stderr)
                break
            }

            val output = result.stdout.trim()
            if (output != testCase.expectedOutput.trim()) {
                logger.error(
                    "Test case wrong: testCaseId={}, submissionId={}, problemId={}",
                    testCase.id,
                    event.submissionId,
                    event.problemId
                )
                break
            }
            logger.info("Test case passed: testCaseId={}, problemId={}", testCase.id, event.problemId)
        }
        logger.info("Submission passed: submissionId={}, problemId={}", event.submissionId, event.problemId)
    }
}