package com.codeloom.executor.service

import com.codeloom.executor.event.SubmissionKafkaEvent
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Service
import tools.jackson.core.JacksonException
import tools.jackson.databind.ObjectMapper


@Service
class SubmissionKafkaListenerService(
    private val objectMapper: ObjectMapper,
    private val submissionProcessingService: SubmissionProcessingService,
) {
    private val logger = LoggerFactory.getLogger(SubmissionKafkaListenerService::class.java)

    @KafkaListener(topics = ["submissions"], groupId = "codeloom")
    fun listenSubmission(message: String) {
        try {
            val event = objectMapper.readValue(message, SubmissionKafkaEvent::class.java)
            logger.info("Received submission event: problemId=${event.problemId} userId=${event.userId}")
            submissionProcessingService.process(event)
        } catch (e: JacksonException) {
            logger.error("Failed to parse event: ", e)
        }
    }
}