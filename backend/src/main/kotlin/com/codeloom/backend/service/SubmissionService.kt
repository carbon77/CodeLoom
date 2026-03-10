package com.codeloom.backend.service

import com.codeloom.backend.dao.ProblemRepository
import com.codeloom.backend.dao.SendSubmissionRequest
import com.codeloom.backend.dao.SubmissionRepository
import com.codeloom.backend.event.SubmissionEvent
import com.codeloom.backend.model.Submission
import com.codeloom.backend.model.SubmissionStatus
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.security.Principal
import java.util.*

@Service
class SubmissionService(
    private val submissionRepository: SubmissionRepository,
    private val problemRepository: ProblemRepository,
    private val kafkaTemplate: KafkaTemplate<String, String>,
    private val objectMapper: ObjectMapper,
    @Value("\${codeloom.kafka.submission-topic}")
    private val submissionsTopic: String,
) {
    fun findSubmissions(problemId: Long, principal: Principal): Collection<Submission> {
        return submissionRepository.findByUserIdAndProblemId(
            userId = UUID.fromString(principal.name),
            problemId = problemId,
        )
    }

    fun sendSubmission(request: SendSubmissionRequest, principal: Principal) {
        if (!problemRepository.existsById(request.problemId)) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Problem with id=${request.problemId} does not exist")
        }

        val submission = submissionRepository.save(
            Submission(
                userId = UUID.fromString(principal.name),
                problemId = request.problemId,
                language = request.language,
                code = request.code,
                status = SubmissionStatus.PENDING,
            )
        )

        val event = SubmissionEvent(
            submissionId = submission.id!!,
            userId = submission.userId,
            problemId = request.problemId,
            code = request.code,
            language = request.language,
        )
        kafkaTemplate.send(
            submissionsTopic,
            submission.id.toString(),
            objectMapper.writeValueAsString(event),
        )
    }
}