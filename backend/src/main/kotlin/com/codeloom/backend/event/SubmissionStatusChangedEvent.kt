package com.codeloom.backend.event

import com.codeloom.backend.model.SubmissionStatus
import java.util.UUID

data class SubmissionStatusChangedEvent(
    val submissionId: UUID,
    val userId: UUID,
    val problemId: Long,
    val newStatus: SubmissionStatus,
    val payload: SubmissionStatusPayload? = null,
)

data class SubmissionStatusPayload(
    val error: String? = null,
    val testCaseResults: List<TestCaseResult>? = null,
)

data class TestCaseResult(
    val id: UUID,
    val problemId: Long,
    val input: String,
    val expectedOutput: String,
    val stdout: String,
    val stderr: String,
    val executionTimeMs: Long,
    val memoryUsageBytes: Long,
)
