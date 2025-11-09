package com.codeloom.backend.model.dao

import com.codeloom.backend.dto.ProblemListItem
import com.codeloom.backend.model.ProblemDifficulty
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import java.util.*

@Repository
class ProblemFilterRepository(
    private val jdbcTemplate: NamedParameterJdbcTemplate,
) {
    companion object {
        val logger: Logger = LoggerFactory.getLogger(ProblemFilterRepository::class.java)
    }

    fun findByFilters(
        difficulties: List<ProblemDifficulty>?,
    ): List<ProblemListItem> {
        val clauses = mutableListOf<String>()
        val params = MapSqlParameterSource()

        if (difficulties != null) {
            val clause = "p.difficulty IN (:difficulties)"
            clauses.add(clause)
            params.addValue("difficulties", difficulties.map { it.toString() })
        }

        val whereClause = if (clauses.isEmpty()) "" else {
            "WHERE " + clauses.joinToString(" and ") { "($it)" }
        }
        val sql = """
            SELECT problem_id, name, difficulty
            FROM problems p
            $whereClause
        """.trimMargin()
        return jdbcTemplate.query(sql, params) { rs, _ ->
            ProblemListItem(
                id = UUID.fromString(rs.getString("problem_id")),
                name = rs.getString("name"),
                difficulty = ProblemDifficulty.valueOf(rs.getString("difficulty")),
            )
        }
    }
}