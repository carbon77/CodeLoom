package com.codeloom.backend.dao.problem

import com.codeloom.backend.dao.testcase.TestCaseRepository
import com.codeloom.backend.dao.topic.TopicRepository
import com.codeloom.backend.dto.ProblemDto
import com.codeloom.backend.dto.ProblemFilters
import com.codeloom.backend.dto.ProblemListDto
import com.codeloom.backend.jooq.tables.references.PROBLEMS
import com.codeloom.backend.jooq.tables.references.PROBLEM_TOPICS
import com.codeloom.backend.jooq.tables.references.TOPICS
import com.codeloom.backend.model.ProblemConstraints
import com.codeloom.backend.model.ProblemDifficulty
import com.codeloom.backend.model.ProblemExamples
import org.jooq.Condition
import org.jooq.DSLContext
import org.springframework.stereotype.Repository
import tools.jackson.databind.ObjectMapper

@Repository
class ProblemRepositoryCustomImpl(
    private val topicRepository: TopicRepository,
    private val testCaseRepository: TestCaseRepository,
    private val dsl: DSLContext,
    private val objectMapper: ObjectMapper,
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

    override fun findProblemDtoBySlug(slug: String): ProblemDto? {
        val dto =
            dsl
                .selectFrom(PROBLEMS)
                .where(PROBLEMS.SLUG.eq(slug))
                .fetchOne() ?: return null

        val problemId = dto.problemId?.toLong() ?: return null
        val testCases = testCaseRepository.findAllByProblemId(problemId, true)
        val topics = topicRepository.findByProblemId(problemId)

        return ProblemDto(
            id = problemId,
            slug = dto.slug ?: "",
            title = dto.title ?: "",
            description = dto.description ?: "",
            difficulty = ProblemDifficulty.valueOf(dto.difficulty!!.literal),
            constraints =
                dto.constraints?.data()?.let {
                    objectMapper.readValue(it, ProblemConstraints::class.java)
                },
            examples =
                dto.examples?.data()?.let {
                    objectMapper.readValue(it, ProblemExamples::class.java)
                },
            hints = dto.hints?.mapNotNull { it }?.toTypedArray()!!,
            testCases = testCases,
            topics = topics,
        )
    }
}
