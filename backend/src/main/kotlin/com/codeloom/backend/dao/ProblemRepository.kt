package com.codeloom.backend.dao

import com.codeloom.backend.model.Problem
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface ProblemRepository : CrudRepository<Problem, Long> {
    fun findBySlug(slug: String): Problem?
}