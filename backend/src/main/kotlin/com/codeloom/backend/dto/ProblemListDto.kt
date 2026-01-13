package com.codeloom.backend.dto

import com.codeloom.backend.model.ProblemDifficulty
import java.sql.ResultSet
import java.time.Instant

data class ProblemListDto(
    val id: Long,
    val title: String,
    val slug: String,
    val difficulty: ProblemDifficulty,
    val publishedAt: Instant? = null,
)

fun ResultSet.toProblemDto() =
    ProblemListDto(
        id = getLong("problem_id"),
        slug = getString("slug"),
        title = getString("title"),
        difficulty = ProblemDifficulty.valueOf(getString("difficulty")),
        publishedAt = getTimestamp("published_at")?.toInstant(),
    )