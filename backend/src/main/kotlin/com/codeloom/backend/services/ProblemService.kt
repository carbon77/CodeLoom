package com.codeloom.backend.services

import com.codeloom.backend.dto.CreateProblemRequest
import com.codeloom.backend.dto.ProblemListItem
import com.codeloom.backend.model.Problem
import com.codeloom.backend.model.ProblemDifficulty
import com.codeloom.backend.model.dao.ProblemFilterRepository
import com.codeloom.backend.model.dao.ProblemRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*
import kotlin.jvm.optionals.getOrNull

@Service
class ProblemService(
    private val problemRepository: ProblemRepository,
    private val problemFilterRepository: ProblemFilterRepository
) {
    @Transactional(readOnly = true)
    fun findByFilters(
        difficulties: List<ProblemDifficulty>? = null,
    ): Iterable<ProblemListItem> {
        return problemFilterRepository.findByFilters(difficulties)
    }

    @Transactional(readOnly = true)
    fun findById(id: UUID): Problem? {
        return problemRepository.findById(id).getOrNull()
    }

    @Transactional
    fun deleteById(id: UUID) {
        problemRepository.deleteById(id)
    }

    @Transactional
    fun create(request: CreateProblemRequest): Problem {
        val problem = Problem(name = request.name)
        return problemRepository.save(problem)
    }

    @Transactional
    fun update(problem: Problem): Problem {
        return problemRepository.save(problem)
    }
}