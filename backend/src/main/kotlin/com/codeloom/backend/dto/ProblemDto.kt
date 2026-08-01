package com.codeloom.backend.dto

import com.codeloom.backend.model.ProblemConstraints
import com.codeloom.backend.model.ProblemDifficulty
import com.codeloom.backend.model.ProblemExamples
import com.codeloom.backend.model.TestCase
import com.codeloom.backend.model.Topic

/**
 * DTO for [com.codeloom.backend.model.Problem]
 */
data class ProblemDto(
    val id: Long?,
    val slug: String,
    val title: String,
    val description: String,
    val difficulty: ProblemDifficulty,
    val constraints: ProblemConstraints?,
    val examples: ProblemExamples?,
    val hints: Array<String>,
    val testCases: Iterable<TestCase>,
    val topics: Iterable<Topic>,
)
