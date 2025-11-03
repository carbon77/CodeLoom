package com.codeloom.backend.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.util.*

@Table("problems")
data class Problem(
    @Id
    @Column("problem_id")
    val id: UUID,
    val name: String,
    val text: String,
    val difficulty: Int,
    val constraints: List<String>,
    val hints: List<String>,
)
