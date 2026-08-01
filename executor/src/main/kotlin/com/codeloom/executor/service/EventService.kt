package com.codeloom.executor.service

import com.codeloom.executor.engine.SubmissionContext
import com.codeloom.executor.engine.SubmissionStatus
import com.codeloom.executor.event.SubmissionStatusChangedEvent
import com.codeloom.executor.event.SubmissionStatusPayload
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import tools.jackson.module.kotlin.jacksonObjectMapper

@Service
class EventService(
    private val kafkaTemplate: KafkaTemplate<String, String>,
    @Value("\${codeloom.kafka.topics.submission-status}")
    private val submissionStatusTopic: String,
) {
    fun submissionStatusChanged(
        context: SubmissionContext,
        newStatus: SubmissionStatus,
        payload: SubmissionStatusPayload? = null,
    ) {
        val event =
            SubmissionStatusChangedEvent(
                submissionId = context.submissionId,
                userId = context.userId,
                problemId = context.problemId,
                newStatus = newStatus,
                payload = payload,
            )

        kafkaTemplate.send(
            submissionStatusTopic,
            context.submissionId.toString(),
            jacksonObjectMapper().writeValueAsString(event),
        )
    }
}
