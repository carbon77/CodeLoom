package com.codeloom.backend.dao

import com.codeloom.backend.dao.filters.AbstractFilterRepository
import com.codeloom.backend.dao.filters.FilterBuilder
import com.codeloom.backend.dao.filters.InFilter
import com.codeloom.backend.dao.mappers.ProblemRowMapper
import com.codeloom.backend.dto.ProblemListItem
import com.codeloom.backend.model.Problem
import com.codeloom.backend.model.ProblemDifficulty
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import java.util.*


@Repository
class ProblemFilterRepository(
    jdbcTemplate: NamedParameterJdbcTemplate,
) : AbstractFilterRepository(jdbcTemplate) {

    fun findItemsByFilters(
        difficulties: List<ProblemDifficulty>?,
    ): List<ProblemListItem> {
        return queryForList(
            tableName = "problems",
            columns = listOf("problem_id", "name", "difficulty"),
            filters = FilterBuilder()
                .addFilter(InFilter("difficulty", difficulties))
                .build(),
            rowMapper = { rs, _ ->
                ProblemListItem(
                    id = UUID.fromString(rs.getString("problem_id")),
                    name = rs.getString("name"),
                    difficulty = ProblemDifficulty.valueOf(rs.getString("difficulty")),
                )
            }
        )
    }

    fun findByFilters(
        difficulties: List<ProblemDifficulty>?,
    ): List<Problem> {
        return queryForList(
            tableName = "problems",
            rowMapper = ProblemRowMapper(),
            filters = FilterBuilder()
                .addFilter(InFilter("difficulty", difficulties))
                .build(),
        )
    }
}