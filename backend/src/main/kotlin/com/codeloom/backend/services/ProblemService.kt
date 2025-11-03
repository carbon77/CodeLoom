package com.codeloom.backend.services

import com.codeloom.backend.model.Problem
import com.codeloom.backend.model.dao.ProblemRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*
import kotlin.jvm.optionals.getOrNull

@Service
class ProblemService(
    private val problemRepository: ProblemRepository,
) {
    @Transactional(readOnly = true)
    fun findAll(): Iterable<Problem> {
        return problemRepository.findAll()
    }

    @Transactional(readOnly = true)
    fun findById(id: UUID): Problem? {
        return problemRepository.findById(id).getOrNull()
    }

    @Transactional
    fun deleteById(id: UUID) {
        problemRepository.deleteById(id)
    }
}