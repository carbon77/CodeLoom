package com.codeloom.backend.dto

import com.codeloom.backend.model.ProblemDifficulty

data class ProblemFilters(
    val difficulties: Set<ProblemDifficulty>? = null,
    val publishedOnly: Boolean = true,
)