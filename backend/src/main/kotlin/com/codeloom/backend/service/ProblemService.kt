package com.codeloom.backend.service

import com.codeloom.backend.config.isRegularUser
import com.codeloom.backend.dao.problem.ProblemRepository
import com.codeloom.backend.dao.testcase.TestCaseRepository
import com.codeloom.backend.dto.CreateProblemRequest
import com.codeloom.backend.dto.ProblemDto
import com.codeloom.backend.dto.ProblemFilters
import com.codeloom.backend.dto.ProblemListDto
import com.codeloom.backend.exception.ForbiddenActionException
import com.codeloom.backend.exception.NoTestCasesException
import com.codeloom.backend.exception.ProblemNotFoundException
import com.codeloom.backend.model.Problem
import com.codeloom.backend.transformer.ProblemTransformer
import org.springframework.dao.DuplicateKeyException
import org.springframework.http.HttpStatus
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import tools.jackson.databind.JsonNode
import java.time.Instant

@Service
class ProblemService(
    private val problemRepository: ProblemRepository,
    private val problemTransformer: ProblemTransformer,
    private val testCaseRepository: TestCaseRepository,
    private val topicService: TopicService,
) {
    @Transactional(readOnly = true)
    fun findItemsByFilters(
        auth: Authentication,
        filters: ProblemFilters,
    ): List<ProblemListDto> {
        if (auth.isRegularUser() && !filters.publishedOnly) {
            throw ForbiddenActionException()
        }
        return problemRepository.findProblemListDtos(filters)
    }

    @Transactional(readOnly = true)
    fun findDtoBySlug(
        auth: Authentication,
        slug: String,
    ): ProblemDto {
        val problem = problemRepository.findBySlug(slug)
        if (problem == null || (auth.isRegularUser() && !problem.isPublished())) {
            throw if (problem == null) {
                ResponseStatusException(HttpStatus.NOT_FOUND, "Problem with slug $slug not found")
            } else {
                ProblemNotFoundException(problem.id!!)
            }
        }

        val problemDto = problemTransformer.getDtoFromEntity(problem)
        return problemDto
    }

    @Transactional(readOnly = true)
    fun findById(
        auth: Authentication,
        id: Long,
    ): Problem {
        val problem = findOrThrow(id)
        if (auth.isRegularUser() && !problem.isPublished()) {
            throw ProblemNotFoundException(id)
        }
        return problem
    }

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

        return try {
            problemRepository.save(problem)
        } catch (e: DuplicateKeyException) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Problem already exists")
        }
    }

    @Transactional
    fun update(
        problemId: Long,
        patchNode: JsonNode,
    ): Problem {
        val problem = findOrThrow(problemId)
        val updated = problemTransformer.updateEntityFromPatchNode(problem, patchNode)

        if (patchNode.has("topics")) {
            topicService.createManyWithProblem(problemId, patchNode.get("topics"))
        }

        try {
            return problemRepository.save(updated)
        } catch (e: DuplicateKeyException) {
            // User can update slug to already existed slug, we need to catch this
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Problem already exists")
        }
    }

    @Transactional
    fun publish(problemId: Long) {
        val problem = findOrThrow(problemId)

        if (testCaseRepository.countAllByProblemId(problemId) == 0) {
            throw NoTestCasesException(problemId)
        }

        problemRepository.save(
            problem.copy(
                publishedAt = Instant.now(),
            ),
        )
    }

    @Transactional
    fun unpublish(problemId: Long) {
        val problem = findOrThrow(problemId)
        problemRepository.save(
            problem.copy(
                publishedAt = null,
            ),
        )
    }

    private fun findOrThrow(problemId: Long): Problem {
        return problemRepository.findById(problemId)
            .orElseThrow { ProblemNotFoundException(problemId) }
    }
}
