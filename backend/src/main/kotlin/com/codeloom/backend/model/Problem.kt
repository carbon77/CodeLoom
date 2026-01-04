package com.codeloom.backend.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant

@Table("problems")
data class Problem(
    @Id
    @Column("problem_id")
    val id: Long? = null,
    @Column("slug")
    val slug: String,
    @Column("title")
    val title: String,
    @Column("description")
    val description: String = "",
    @Column("difficulty")
    val difficulty: ProblemDifficulty = ProblemDifficulty.EASY,
    @Column("constraints")
    val constraints: ProblemConstraints? = null,
    @Column("examples")
    val examples: ProblemExamples? = null,
    @Column("hints")
    val hints: Array<String> = arrayOf(),
    @Column("created_at")
    val createdAt: Instant = Instant.now(),
    @Column("updated_at")
    val updatedAt: Instant = Instant.now(),
    @Column("published_at")
    val publishedAt: Instant? = null,
)
