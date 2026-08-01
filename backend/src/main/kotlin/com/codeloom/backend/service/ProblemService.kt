package com.codeloom.backend.service

import com.codeloom.backend.dao.ProblemQueryRepository
import com.codeloom.backend.dao.ProblemRepository
import com.codeloom.backend.dto.CreateProblemRequest
import com.codeloom.backend.dto.ProblemDto
import com.codeloom.backend.dto.ProblemFilters
import com.codeloom.backend.dto.ProblemListDto
import com.codeloom.backend.model.Problem
import com.codeloom.backend.patchValue
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import tools.jackson.databind.JsonNode
import tools.jackson.databind.ObjectMapper
import java.time.Instant

@Service
class ProblemService(
    private val topicService: TopicService,
    private val problemRepository: ProblemRepository,
    private val problemQueryRepository: ProblemQueryRepository,
    private val objectMapper: ObjectMapper,
) {
    @Transactional(readOnly = true)
    fun findItemsByFilters(filters: ProblemFilters): List<ProblemListDto> =
        problemQueryRepository.findProblemListDtos(filters)

    @Transactional(readOnly = true)
    fun findDtoBySlug(slug: String): ProblemDto =
        problemQueryRepository.findProblemDtoBySlug(slug)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Problem with slug $slug not found")

    @Transactional(readOnly = true)
    fun findById(id: Long): Problem =
        problemRepository
            .findById(id)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Problem with ID $id not found") }

    @Transactional
    fun deleteById(id: Long) {
        problemRepository.deleteById(id)
    }

    @Transactional
    fun create(request: CreateProblemRequest): Problem {
        val problem =
            Problem(
                title = request.title,
                slug = request.title.lowercase().replace(" ", "_"),
            )
        return problemRepository.save(problem)
    }

    @Transactional
    fun update(
        problemId: Long,
        patchNode: JsonNode,
    ): Problem {
        val problem =
            problemRepository
                .findById(problemId)
                .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Problem with ID $problemId not found") }
        val updated =
            problem.copy(
                title = patchNode.patchValue("title", objectMapper, problem.title),
                slug = patchNode.patchValue("slug", objectMapper, problem.slug),
                description = patchNode.patchValue("description", objectMapper, problem.description),
                difficulty = patchNode.patchValue("difficulty", objectMapper, problem.difficulty),
                hints =
                    patchNode
                        .get("hints")
                        ?.takeIf { it.isArray }
                        ?.asArray()
                        ?.values()
                        ?.map { it.asString() }
                        ?.toTypedArray()
                        ?: problem.hints,
                examples = patchNode.patchValue("examples", objectMapper, problem.examples),
                constraints = patchNode.patchValue("constraints", objectMapper, problem.constraints),
            )

        if (patchNode.has("topics")) {
            topicService.createManyWithProblem(problemId, patchNode.get("topics"))
        }

        return problemRepository.save(updated)
    }

    @Transactional
    fun publish(problemId: Long) {
        val problem = findById(problemId)
        problemRepository.save(
            problem.copy(
                publishedAt = Instant.now(),
            ),
        )
    }

    @Transactional
    fun unpublish(problemId: Long) {
        val problem = findById(problemId)
        problemRepository.save(
            problem.copy(
                publishedAt = null,
            ),
        )
    }
}
