package com.codeloom.backend.service

import com.codeloom.backend.config.getUserId
import com.codeloom.backend.config.hasRole
import com.codeloom.backend.dao.SubmissionRepository
import com.codeloom.backend.dao.problem.ProblemRepository
import com.codeloom.backend.dto.SendSubmissionRequest
import com.codeloom.backend.exception.ProblemNotFoundException
import com.codeloom.backend.model.Submission
import com.codeloom.backend.security.UserRole
import com.codeloom.common.SubmissionEvent
import com.codeloom.common.SubmissionStatus
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import tools.jackson.databind.ObjectMapper

@Service
class SubmissionService(
    private val submissionRepository: SubmissionRepository,
    private val problemRepository: ProblemRepository,
    private val kafkaTemplate: KafkaTemplate<String, String>,
    private val objectMapper: ObjectMapper,
    @Value("\${codeloom.kafka.submission-topic}")
    private val submissionsTopic: String,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    fun findSubmissions(
        problemId: Long,
        auth: Authentication,
    ): Collection<Submission> {
        return submissionRepository.findByUserIdAndProblemId(
            userId = auth.getUserId(),
            problemId = problemId,
        )
    }

    @Transactional
    fun sendSubmission(
        request: SendSubmissionRequest,
        auth: Authentication,
    ) {
        logger.info("Sending submission...")
        val problem = problemRepository.findById(request.problemId).orElseThrow {
            ProblemNotFoundException(request.problemId)
        }

        if (auth.hasRole(UserRole.USER) && !problem.isPublished()) {
            // We don't want to expose information about unpublished problems
            throw ProblemNotFoundException(request.problemId)
        }

        val submission =
            submissionRepository.save(
                Submission(
                    userId = auth.getUserId(),
                    problemId = request.problemId,
                    language = request.language,
                    code = request.code,
                    status = SubmissionStatus.PENDING,
                ),
            )

        val event =
            SubmissionEvent(
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
        logger.info("Submission sent: submissionId={}", submission.id)
    }
}
