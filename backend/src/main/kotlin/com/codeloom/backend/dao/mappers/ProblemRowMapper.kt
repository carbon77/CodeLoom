package com.codeloom.backend.dao.mappers

import com.codeloom.backend.model.Problem
import com.codeloom.backend.model.ProblemDifficulty
import org.springframework.jdbc.core.RowMapper
import java.sql.ResultSet
import java.time.Instant
import java.util.*

class ProblemRowMapper : RowMapper<Problem> {
    override fun mapRow(rs: ResultSet, rowNum: Int): Problem? {
        return Problem(
            id = UUID.fromString(rs.getString("problem_id")),
            name = rs.getString("name"),
            text = rs.getString("text"),
            difficulty = ProblemDifficulty.valueOf(rs.getString("difficulty")),
            constraints = parseArray(rs.getString("constraints")),
            hints = parseArray(rs.getString("hints")),
            createdAt = rs.getTimestamp("created_at")?.toInstant() ?: Instant.now(),
            updatedAt = rs.getTimestamp("updated_at")?.toInstant() ?: Instant.now(),
            publishedAt = rs.getTimestamp("published_at")?.toInstant(),
        )
    }

    private fun parseArray(str: String): List<String> {
        if (str.isEmpty() || str == "{}") return emptyList()
        return str
            .removePrefix("{")
            .removeSuffix("}")
            .split(",")
            .map { it.trim() }
    }
}