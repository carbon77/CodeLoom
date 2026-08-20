package com.codeloom.backend.dao.testcase

import com.codeloom.backend.jooq.tables.references.TEST_CASES
import com.codeloom.backend.model.TestCase
import org.jooq.DSLContext
import org.springframework.stereotype.Repository

@Repository
class TestCaseRepositoryCustomImpl(
    private val dsl: DSLContext,
) : TestCaseRepositoryCustom {
    override fun findAllByProblemId(
        problemId: Long,
        isPublic: Boolean?,
    ): Collection<TestCase> {
        val conditions =
            mutableListOf(
                TEST_CASES.PROBLEM_ID.eq(problemId.toInt()),
            )
        isPublic?.let {
            conditions.add(TEST_CASES.IS_PUBLIC.eq(isPublic))
        }
        val stmt =
            dsl.select(TEST_CASES.asterisk())
                .from(TEST_CASES)
                .where(conditions)
        return stmt.fetchInto(TestCase::class.java)
    }

    override fun countAllByProblemId(problemId: Long, isPublic: Boolean?): Int {
        val conditions =
            mutableListOf(
                TEST_CASES.PROBLEM_ID.eq(problemId.toInt()),
            )
        isPublic?.let {
            conditions.add(TEST_CASES.IS_PUBLIC.eq(isPublic))
        }
        val stmt =
            dsl.selectCount()
                .from(TEST_CASES)
                .where(conditions)
        return stmt.fetchOne(0, Int::class.java) ?: 0
    }
}
