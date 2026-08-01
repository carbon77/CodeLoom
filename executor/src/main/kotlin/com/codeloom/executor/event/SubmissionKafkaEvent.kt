package com.codeloom.executor.event

import java.util.UUID

data class SubmissionKafkaEvent(
    val submissionId: UUID,
    val userId: UUID,
    val problemId: Long,
    val code: String,
    val language: String,
    val executionTimeLimitMs: Long? = null,
    val memoryUsageLimitBytes: Long? = null,
)
