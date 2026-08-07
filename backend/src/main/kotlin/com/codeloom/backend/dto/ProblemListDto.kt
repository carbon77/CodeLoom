package com.codeloom.backend.dto

import com.codeloom.backend.model.ProblemDifficulty
import java.time.Instant

data class ProblemListDto(
    val id: Long,
    val title: String,
    val slug: String,
    val difficulty: ProblemDifficulty,
    val publishedAt: Instant? = null,
)
