package com.codeloom.backend.dto

import com.codeloom.backend.model.ProblemDifficulty
import java.util.*

data class ProblemListItem(
    val id: UUID,
    val name: String,
    val difficulty: ProblemDifficulty
)