package com.codeloom.backend.dao

import com.codeloom.backend.dto.ProblemDto
import com.codeloom.backend.dto.ProblemFilters
import com.codeloom.backend.dto.ProblemListDto
import com.codeloom.backend.dto.toProblemDto
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository

@Repository
class ProblemQueryRepository(
    private val jdbc: NamedParameterJdbcTemplate,
    private val problemRepository: ProblemRepository,
    private val topicRepository: TopicRepository,
    private val testCaseRepository: TestCaseRepository,
) {

    fun findProblemListDtos(filters: ProblemFilters): List<ProblemListDto> {
        val sql = StringBuilder(
            """
            SELECT
                p.problem_id,
                p.slug,
                p.title,
                p.difficulty,
                p.published_at
            FROM problems p
        """.trimIndent()
        )
        val joinClause = StringBuilder()
        val whereClause = StringBuilder(" WHERE 1 = 1")
        val params = mutableMapOf<String, Any>()

        filters.difficulties?.takeIf { it.isNotEmpty() }?.let {
            whereClause.append(" AND difficulty IN (:difficulties)")
            params["difficulties"] = it.map { difficulty -> difficulty.name }
        }

        filters.topics?.takeIf { it.isNotEmpty() }?.let {
            joinClause.append(" INNER JOIN problem_topics pt ON pt.problem_id = p.problem_id")
            joinClause.append(" INNER JOIN topics t ON t.topic_id = pt.topic_id")
            whereClause.append(" AND t.name IN (:topics)")
            params["topics"] = filters.topics
        }

        if (filters.publishedOnly) {
            whereClause.append(" AND published_at IS NOT NULL")
        }

        sql.append(whereClause.toString())

        return jdbc.query(sql.toString(), params) { rs, _ ->
            rs.toProblemDto()
        }
    }

    fun findProblemDtoBySlug(slug: String): ProblemDto? {
        val problem = problemRepository.findBySlug(slug) ?: return null
        if (problem.id == null) return null
        val testCases = testCaseRepository.findByProblemIdAndIsPublic(problem.id, true)
        val topics = topicRepository.findByProblemId(problem.id)

        return ProblemDto(
            id = problem.id,
            slug = problem.slug,
            title = problem.title,
            description = problem.description,
            difficulty = problem.difficulty,
            hints = problem.hints,
            constraints = problem.constraints,
            examples = problem.examples,
            testCases = testCases,
            topics = topics,
        )
    }
}