package com.codeloom.backend.event

import java.util.UUID

data class SubmissionEvent(
    val submissionId: UUID,
    val userId: UUID,
    val problemId: Long,
    val code: String,
    val language: String,
)
