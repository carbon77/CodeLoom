package com.codeloom.executor.event

import com.codeloom.executor.engine.SubmissionStatus
import java.util.*

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
