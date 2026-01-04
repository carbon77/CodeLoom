package com.codeloom.backend.dao

import com.codeloom.backend.dto.ProblemFilters
import com.codeloom.backend.dto.ProblemListItem
import com.codeloom.backend.dto.toProblemListItem
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository

@Repository
class ProblemQueryRepository(
    private val jdbc: NamedParameterJdbcTemplate,
) {

    fun findProblems(filters: ProblemFilters): List<ProblemListItem> {
        val sql = StringBuilder(
            """
            SELECT
             problem_id,
             slug,
             title,
             difficulty,
             published_at
            FROM problems
            WHERE 1 = 1
        """.trimIndent()
        )
        val params = mutableMapOf<String, Any>()

        filters.difficulties?.takeIf { it.isNotEmpty() }?.let {
            sql.append(" AND difficulty IN (:difficulties)")
            params["difficulties"] = it.map { difficulty -> difficulty.name }
        }

        if (filters.publishedOnly) {
            sql.append(" AND published_at IS NOT NULL")
        }

        return jdbc.query(sql.toString(), params) { rs, _ ->
            rs.toProblemListItem()
        }
    }
}