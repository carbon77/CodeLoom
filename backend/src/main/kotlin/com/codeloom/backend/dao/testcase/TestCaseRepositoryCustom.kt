package com.codeloom.backend.dao.testcase

import com.codeloom.backend.model.TestCase

interface TestCaseRepositoryCustom {
    fun findAllByProblemId(
        problemId: Long,
        isPublic: Boolean?,
    ): Iterable<TestCase>
}
