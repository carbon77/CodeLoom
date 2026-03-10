package com.codeloom.backend.model

import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant
import java.util.UUID

@Table("submissions")
data class Submission(
    @Id
    @Column("submission_id")
    val id: UUID? = null,

    @Column("user_id")
    val userId: UUID,
    @Column("problem_id")
    val problemId: Long,
    @Column("code")
    val code: String,
    @Column("status")
    val status: SubmissionStatus,
    @Column("language")
    val language: String,

    @CreatedDate
    @Column("created_at")
    val createdAt: Instant = Instant.now(),
)
