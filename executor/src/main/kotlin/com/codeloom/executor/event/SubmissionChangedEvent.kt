package com.codeloom.executor.event

import java.util.UUID

data class SubmissionChangedEvent(
    val submissionId: UUID,
    val userId: UUID,
    val problemId: Long,
)
