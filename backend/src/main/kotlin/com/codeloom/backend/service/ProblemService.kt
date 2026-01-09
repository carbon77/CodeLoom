package com.codeloom.backend.service

import com.codeloom.backend.dao.ProblemQueryRepository
import com.codeloom.backend.dao.ProblemRepository
import com.codeloom.backend.dto.CreateProblemRequest
import com.codeloom.backend.dto.ProblemFilters
import com.codeloom.backend.dto.ProblemListItem
import com.codeloom.backend.model.Problem
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.time.Instant

@Service
class ProblemService(
    private val problemRepository: ProblemRepository,
    private val problemQueryRepository: ProblemQueryRepository,
) {
    @Transactional(readOnly = true)
    fun findItemsByFilters(filters: ProblemFilters): List<ProblemListItem> {
        return problemQueryRepository.findProblems(filters)
    }

    @Transactional(readOnly = true)
    fun findById(id: Long): Problem {
        return problemRepository.findById(id)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Problem with ID $id not found") }
    }

    @Transactional(readOnly = true)
    fun findBySlug(slug: String): Problem {
        return problemRepository.findBySlug(slug)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Problem with slug $slug not found")
    }

    @Transactional
    fun deleteById(id: Long) {
        problemRepository.deleteById(id)
    }

    @Transactional
    fun create(request: CreateProblemRequest): Problem {
        val problem = Problem(
            title = request.title,
            slug = request.title.lowercase().replace(" ", "_"),
        )
        return problemRepository.save(problem)
    }

    @Transactional
    fun update(problemId: Long, updated: Problem): Problem {
        val old = findById(problemId)
        return problemRepository.save(
            old.copy(
                title = updated.title,
                slug = updated.slug,
                description = updated.description,
                hints = updated.hints,
                examples = updated.examples,
                constraints = updated.constraints,
            )
        )
    }

    @Transactional
    fun publish(problemId: Long) {
        val problem = findById(problemId)
        problemRepository.save(
            problem.copy(
                publishedAt = Instant.now(),
            )
        )
    }

    @Transactional
    fun unpublish(problemId: Long) {
        val problem = findById(problemId)
        problemRepository.save(
            problem.copy(
                publishedAt = null,
            )
        )
    }
}