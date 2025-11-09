package com.codeloom.backend.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant
import java.util.*

@Table("problems")
data class Problem(
    @Id
    @Column("problem_id")
    val id: UUID? = null,
    val name: String,
    val text: String = "",
    val difficulty: ProblemDifficulty = ProblemDifficulty.EASY,
    val constraints: List<String> = listOf(),
    val hints: List<String> = listOf(),
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now(),
    val publishedAt: Instant? = null,
)
