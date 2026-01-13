package com.codeloom.backend.dao;

import com.codeloom.backend.model.TestCase
import org.springframework.data.repository.CrudRepository
import java.util.UUID

interface TestCaseRepository : CrudRepository<TestCase, UUID> {
    fun findByProblemIdAndIsPublic(problemId: Long, isPublic: Boolean): Iterable<TestCase>
    fun findByProblemId(problemId: Long): Iterable<TestCase>
}