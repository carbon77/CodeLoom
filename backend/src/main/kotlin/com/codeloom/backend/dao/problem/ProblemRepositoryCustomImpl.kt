package com.codeloom.backend.dao.problem

import com.codeloom.backend.dto.ProblemFilters
import com.codeloom.backend.dto.ProblemListDto
import com.codeloom.backend.jooq.tables.references.PROBLEMS
import com.codeloom.backend.jooq.tables.references.PROBLEM_TOPICS
import com.codeloom.backend.jooq.tables.references.TOPICS
import org.jooq.Condition
import org.jooq.DSLContext
import org.springframework.stereotype.Repository

@Repository
class ProblemRepositoryCustomImpl(
    private val dsl: DSLContext,
) : ProblemRepositoryCustom {
    override fun findProblemListDtos(filters: ProblemFilters): List<ProblemListDto> {
        var stmt =
            dsl.select(
                PROBLEMS.PROBLEM_ID,
                PROBLEMS.SLUG,
                PROBLEMS.TITLE,
                PROBLEMS.DIFFICULTY,
                PROBLEMS.PUBLISHED_AT,
            )
                .from(PROBLEMS)
        val conditions = mutableListOf<Condition>()

        filters.difficulties?.takeIf { it.isNotEmpty() }?.let {
            conditions.add(PROBLEMS.DIFFICULTY.`in`(filters.difficulties))
        }

        filters.topics?.takeIf { it.isNotEmpty() }?.let {
            stmt =
                stmt
                    .innerJoin(PROBLEM_TOPICS)
                    .on(PROBLEM_TOPICS.PROBLEM_ID.eq(PROBLEMS.PROBLEM_ID))
                    .innerJoin(TOPICS)
                    .on(TOPICS.TOPIC_ID.eq(PROBLEM_TOPICS.TOPIC_ID))

            conditions.add(TOPICS.NAME.`in`(filters.topics))
        }

        if (filters.publishedOnly) {
            conditions.add(PROBLEMS.PUBLISHED_AT.isNotNull)
        }

        return stmt.where(conditions)
            .fetchInto(ProblemListDto::class.java)
    }
}
