package com.codeloom.backend.service

import com.codeloom.backend.dao.SubmissionRepository
import com.codeloom.backend.dao.TestCaseResultRepository
import com.codeloom.backend.model.TestCaseResult
import com.codeloom.common.SubmissionStatusChangedEvent
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Service
import tools.jackson.core.JacksonException
import tools.jackson.databind.ObjectMapper

@Service
class SubmissionStatusKafkaListenerService(
    private val submissionRepository: SubmissionRepository,
    private val testCaseResultRepository: TestCaseResultRepository,
    private val objectMapper: ObjectMapper,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @KafkaListener(topics = ["\${codeloom.kafka.submission-status-topic}"])
    fun listenSubmissionStatus(message: String) {
        try {
            val event = objectMapper.readValue(message, SubmissionStatusChangedEvent::class.java)
            val submission = submissionRepository.findById(event.submissionId)
            if (submission.isEmpty) {
                logger.warn("Submission not found: submissionId={}", event.submissionId)
                return
            }

            submissionRepository.save(submission.get().copy(status = event.newStatus))
            logger.info("Submission(id={}) status updated to {}", event.submissionId, event.newStatus)

            val results = event.payload?.testCaseResults
            if (results != null) {
                testCaseResultRepository.deleteBySubmissionId(event.submissionId)
                testCaseResultRepository.saveAll(
                    results.map {
                        TestCaseResult(
                            submissionId = event.submissionId,
                            input = it.input,
                            expectedOutput = it.expectedOutput,
                            stdout = it.stdout,
                            stderr = it.stderr,
                            executionTimeMs = it.executionTimeMs,
                            bytesUsed = it.memoryUsageBytes,
                        )
                    }
                )
                logger.info(
                    "Persisted {} test case result(s) for submission={}",
                    results.size,
                    event.submissionId,
                )
            }
        } catch (e: JacksonException) {
            logger.error("Failed to parse submission status event", e)
        }
    }
}
