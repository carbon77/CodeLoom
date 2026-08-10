package com.codeloom.executor.repository

import com.codeloom.executor.model.TestCase
import org.springframework.data.repository.CrudRepository
import java.util.UUID

interface TestCaseRepository : CrudRepository<TestCase, UUID> {
    fun findByProblemId(problemId: Long): List<TestCase>
}
